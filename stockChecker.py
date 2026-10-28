import json
import re
import time
from pathlib import Path

from playwright.sync_api import sync_playwright


INPUT_FILE = "output.json"
OUTPUT_FILE = "filtered.json"

# Change this to the Systembolaget store you want to check.
STORE_NAME = "SALUHALLEN"


def get_link(product):
    product_type = product["type"].replace("&", "-")
    product_type = re.sub(r"\s", "", product_type)

    name = re.sub(r"\s", "-", product["name"])

    return (
        f"https://www.systembolaget.se/produkt/"
        f"{product_type}/{name}-{product['id']}"
    )


def get_stock(page):
    """
    Returns the number of products available in the selected store.

    Returns:
        int  -> stock count
        None -> stock element wasn't found
    """

    # The class is much more stable than the absolute XPath.
    element = page.locator("span.monopol-250").first

    if element.count() == 0:
        return None

    text = element.inner_text().strip()

    # Examples:
    # "20 st"
    # "1 st"
    # "0 st"
    match = re.search(r"(\d+)", text)

    if not match:
        return None

    return int(match.group(1))


with open(INPUT_FILE, "r", encoding="utf-8") as f:
    products = json.load(f)


kept_products = []
removed_products = []


with sync_playwright() as p:
    browser = p.chromium.launch(headless=False)

    context = browser.new_context(
        locale="sv-SE"
    )

    page = context.new_page()

    # Open Systembolaget first so that you can manually select the store.
    page.goto("https://www.systembolaget.se/", wait_until="domcontentloaded")

    print()
    print("Select the desired store in the browser.")
    print("After selecting it, press ENTER here.")
    input()

    for index, product in enumerate(products, start=1):

        url = get_link(product)

        print(
            f"[{index}/{len(products)}] "
            f"{product['name']} ({product['id']})"
        )

        try:
            page.goto(
                url,
                wait_until="domcontentloaded",
                timeout=30000
            )

            # Give the page/React app time to render the stock information.
            page.wait_for_timeout(1500)

            stock = get_stock(page)

            if stock is None:
                print("  -> No stock element found - REMOVED")
                removed_products.append(product)
                continue

            print(f"  -> {stock} st")

            if stock > 0:
                kept_products.append(product)
                print("  -> KEEP")
            else:
                removed_products.append(product)
                print("  -> REMOVE")

        except Exception as e:
            print(f"  -> ERROR: {e}")
            removed_products.append(product)

        # Be polite to the website.
        time.sleep(0.5)

    browser.close()


with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
    json.dump(
        kept_products,
        f,
        ensure_ascii=False,
        indent=2
    )


print()
print("Finished!")
print(f"Original products: {len(products)}")
print(f"Kept:              {len(kept_products)}")
print(f"Removed:           {len(removed_products)}")
print(f"Output:            {OUTPUT_FILE}")