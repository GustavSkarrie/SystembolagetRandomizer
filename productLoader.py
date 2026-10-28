import json
import re
import time
from urllib.parse import urljoin

from playwright.sync_api import sync_playwright


BASE_URL = "https://www.systembolaget.se"
SORTIMENT_URL = "https://www.systembolaget.se/sortiment/?p={}"
OUTPUT_FILE = "output.json"

PAGE_WAIT_MS = 3000
PAGE_DELAY = 0.5


def get_product_id(href):
    """
    Extract product ID from the product href.

    Example:
        /produkt/vin/crudo-262708/
        -> 262708
    """

    match = re.search(r"-(\d+)/?$", href)

    if not match:
        return None

    return match.group(1)


def get_product_type(href):
    """
    Extract product type from the product href.

    Example:
        /produkt/vin/crudo-262708/
        -> Vin
    """

    parts = href.strip("/").split("/")

    if len(parts) < 2:
        return None

    return parts[1].capitalize()


def get_image_url(tile):
    """
    Get image URL using the ID from:

        id="tile:64267303"

    Result:

        https://product-cdn.systembolaget.se/productimages/
        64267303/64267303_400.png
    """

    tile_id = tile.get_attribute("id")

    if not tile_id:
        return None

    match = re.match(r"tile:(\d+)", tile_id)

    if not match:
        return None

    image_id = match.group(1)

    return (
        f"https://product-cdn.systembolaget.se/"
        f"productimages/{image_id}/{image_id}_400.png"
    )


def get_stock(tile):
    """
    Get stock from the product's store section.

    Example HTML:

        <output
            id="store-section-64267303"
            ...
        >
            ...
            <span class="monopol-250 text-black">16 st</span>
            ...
        </output>

    Returns:
        int  -> stock amount
        None -> stock information could not be found
    """

    # Find the store section belonging to this product.
    store_section = tile.locator(
        '[id^="store-section-"]'
    )

    if store_section.count() == 0:
        print("      Store section not found")
        return None
    
    section = store_section.first
    stock_container = section.locator( 
        "xpath=following-sibling::*[1]" 
    )

    # Specifically target:
    #
    # <span class="monopol-250 text-black">16 st</span>
    #
    stock_element = stock_container.locator(
        "span.monopol-250.text-black"
    )

    if stock_element.count() == 0:
        print("      Stock span not found")
        return None

    text = stock_element.first.inner_text().strip()

    print(f"      Stock text: '{text}'")

    match = re.search(r"(\d+)\s*st", text)

    if not match:
        return None

    return int(match.group(1))


def get_price(tile):
    """
    Extract the product price.

    Examples:
        249:- -> 249.0
        32,90 -> 32.9
    """

    price_element = tile.locator(
        'span[aria-hidden="true"].sans-strong-175'
    )

    if price_element.count() == 0:
        return None

    text = price_element.inner_text().strip()

    text = text.replace(":-", "")
    text = text.replace(" ", "")
    text = text.replace(",", ".")

    match = re.search(
        r"\d+(?:\.\d+)?",
        text
    )

    if not match:
        return None

    return float(match.group(0))


def get_product_name(tile):
    """
    Get product name.
    """

    name_element = tile.locator(
        "span.monopol-250"
    ).first

    if name_element.count() == 0:
        return None

    return name_element.inner_text().strip()


def scrape_page(page, page_number):
    """
    Scrape one Sortiment page.

    Returns:
        None -> page has NO products at all
        list -> page had products, possibly an empty list
                if none had stock
    """

    url = SORTIMENT_URL.format(page_number)

    print()
    print("=" * 70)
    print(f"PAGE {page_number}")
    print("=" * 70)
    print(url)

    page.goto(
        url,
        wait_until="domcontentloaded",
        timeout=30000
    )

    # --------------------------------------------------------------
    # WAIT FOR PRODUCTS
    # --------------------------------------------------------------

    try:
        page.wait_for_selector(
            'li.flex.flex-col:has(a[id^="tile:"])',
            state="attached",
            timeout=15000
        )
    except Exception:
        # There may genuinely be no products on the page.
        pass

    tiles = page.locator(
        'li.flex.flex-col:has(a[id^="tile:"])'
    )

    tile_count = tiles.count()

    print(f"Products on page: {tile_count}")

    # IMPORTANT:
    #
    # Only stop pagination if there are NO product tiles.
    #
    # A page containing products but zero products in stock
    # must still allow pagination to continue.
    if tile_count == 0:
        return None

    # Give Systembolaget's client-side stock loading time.
    page.wait_for_timeout(PAGE_WAIT_MS)

    products = []

    # --------------------------------------------------------------
    # PROCESS PRODUCTS
    # --------------------------------------------------------------

    for i in range(tile_count):

        tile = tiles.nth(i)

        try:

            print()
            print(
                f"  [{i + 1}/{tile_count}] Processing..."
            )

            # ------------------------------------------------------
            # PRODUCT LINK
            # ------------------------------------------------------

            link = tile.locator(
                'a[id^="tile:"]'
            ).first

            href = link.get_attribute("href")

            if not href:
                print("      No href - skipped")
                continue

            # ------------------------------------------------------
            # PRODUCT ID
            # ------------------------------------------------------

            product_id = get_product_id(href)

            if not product_id:
                print("      Could not get product ID - skipped")
                continue

            # ------------------------------------------------------
            # PRODUCT TYPE
            # ------------------------------------------------------

            product_type = get_product_type(href)

            # ------------------------------------------------------
            # NAME
            # ------------------------------------------------------

            name = get_product_name(tile)

            # ------------------------------------------------------
            # PRICE
            # ------------------------------------------------------

            price = get_price(tile)

            # ------------------------------------------------------
            # IMAGE
            # ------------------------------------------------------

            image_url = get_image_url(tile)

            print(
                f"      {name} ({product_id})"
            )

            # ------------------------------------------------------
            # STOCK
            # ------------------------------------------------------

            # Wait specifically for this product's store section.
            store_section = tile.locator(
                '[id^="store-section-"]'
            )

            try:
                store_section.wait_for(
                    state="attached",
                    timeout=10000
                )
            except Exception:
                print(
                    "      Store section did not load - skipped"
                )
                continue

            stock = get_stock(tile)

            if stock is None:
                print(
                    "      Stock information unavailable - skipped"
                )
                continue

            print(
                f"      Stock: {stock} st"
            )

            # No stock at selected store.
            if stock <= 0:
                print(
                    "      OUT OF STOCK - skipped"
                )
                continue

            # ------------------------------------------------------
            # BUILD PRODUCT
            # ------------------------------------------------------

            product = {
                "price": price,
                "name": name,
                "id": product_id,
                "type": product_type,
                "url": image_url
            }

            products.append(product)

            print("      KEEP")

        except Exception as e:

            print(
                f"      ERROR: {e}"
            )

    return products


def main():

    all_products = []

    with sync_playwright() as p:

        browser = p.chromium.launch(
            headless=False
        )

        context = browser.new_context(
            locale="sv-SE"
        )

        page = context.new_page()

        # ----------------------------------------------------------
        # STORE SELECTION
        # ----------------------------------------------------------

        page.goto(
            BASE_URL,
            wait_until="domcontentloaded",
            timeout=30000
        )

        print()
        print("=" * 70)
        print("SELECT SYSTEMBOLAGET STORE")
        print("=" * 70)
        print()
        print(
            "Select the store you want to use in the browser."
        )
        print()
        print(
            "When you have selected the store,"
        )
        print(
            "press ENTER here."
        )
        print()

        input()

        # ----------------------------------------------------------
        # SCRAPE ALL PAGES
        # ----------------------------------------------------------

        page_number = 1

        while True:

            try:

                products = scrape_page(
                    page,
                    page_number
                )

            except Exception as e:

                print()
                print(
                    f"ERROR loading page {page_number}:"
                )
                print(e)
                print()
                print("Stopping scraper.")

                break

            # ------------------------------------------------------
            # ONLY STOP IF THE PAGE HAD ZERO PRODUCTS.
            # ------------------------------------------------------

            if products is None:

                print()
                print(
                    f"No products found on page "
                    f"{page_number}."
                )
                print(
                    "Reached the end of the catalogue."
                )

                break

            # Products existed on this page, so continue even if
            # none of them had stock.
            all_products.extend(products)

            print()
            print(
                f"Page {page_number} finished:"
            )
            print(
                f"  In stock: {len(products)}"
            )
            print(
                f"  Total:    {len(all_products)}"
            )

            page_number += 1

            time.sleep(PAGE_DELAY)

        browser.close()

    # --------------------------------------------------------------
    # REMOVE DUPLICATES
    # --------------------------------------------------------------

    unique_products = {}

    for product in all_products:
        unique_products[product["id"]] = product

    all_products = list(
        unique_products.values()
    )

    # --------------------------------------------------------------
    # SAVE JSON
    # --------------------------------------------------------------

    with open(
        OUTPUT_FILE,
        "w",
        encoding="utf-8"
    ) as f:

        json.dump(
            all_products,
            f,
            ensure_ascii=False,
            indent=2
        )

    print()
    print("=" * 70)
    print("FINISHED")
    print("=" * 70)
    print()
    print(
        f"Products with stock: {len(all_products)}"
    )
    print(
        f"Pages processed:     {page_number - 1}"
    )
    print(
        f"Output:              {OUTPUT_FILE}"
    )
    print()


if __name__ == "__main__":
    main()