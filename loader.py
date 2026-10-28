import json
import tkinter as tk
from tkinter import messagebox
import webbrowser
from urllib.parse import quote

JSON_FILE = "output.json"
ITEMS_PER_PAGE = 15


class ProductViewer:
    def __init__(self, root):
        self.root = root
        self.root.title("Products")
        self.root.geometry("1200x700")

        try:
            with open(JSON_FILE, "r", encoding="utf-8") as f:
                self.products = json.load(f)
        except Exception as e:
            messagebox.showerror("Error", f"Failed to load JSON:\n{e}")
            self.products = []

        self.current_page = 0

        # Main container
        self.container = tk.Frame(root)
        self.container.pack(fill="both", expand=True)

        # Product list area
        self.products_frame = tk.Frame(self.container)
        self.products_frame.pack(fill="both", expand=True)

        # Navigation bar
        nav = tk.Frame(self.container)
        nav.pack(fill="x", pady=10)

        self.prev_btn = tk.Button(
            nav,
            text="← Previous",
            width=12,
            command=self.previous_page
        )
        self.prev_btn.pack(side="left", padx=5)

        self.next_btn = tk.Button(
            nav,
            text="Next →",
            width=12,
            command=self.next_page
        )
        self.next_btn.pack(side="left", padx=5)

        self.last_btn = tk.Button(
            nav,
            text="Last Page >>",
            width=12,
            command=self.last_page
        )
        self.last_btn.pack(side="left", padx=5)

        self.page_label = tk.Label(nav, text="", font=("Arial", 11, "bold"))
        self.page_label.pack(side="left", padx=20)

        self.refresh_page()

    def build_link(self, product):
        product_type = quote(product["type"].lower())
        product_name = quote(product["name"].lower())

        return (
            f"https://www.systembolaget.se/produkt/"
            f"{product_type}/{product_name}-{product['id']}"
        )

    def save_json(self):
        with open(JSON_FILE, "w", encoding="utf-8") as f:
            json.dump(
                self.products,
                f,
                ensure_ascii=False,
                indent=4
            )

    def refresh_page(self):
        # Clear current page
        for widget in self.products_frame.winfo_children():
            widget.destroy()

        total_pages = max(
            1,
            (len(self.products) + ITEMS_PER_PAGE - 1) // ITEMS_PER_PAGE
        )

        start = self.current_page * ITEMS_PER_PAGE
        end = start + ITEMS_PER_PAGE

        for product in self.products[start:end]:
            self.create_product_row(product)

        self.page_label.config(
            text=f"Page {self.current_page + 1} / {total_pages}    "
                 f"Products: {len(self.products)}"
        )

        self.prev_btn.config(
            state="normal" if self.current_page > 0 else "disabled"
        )

        self.next_btn.config(
            state="normal"
            if self.current_page < total_pages - 1
            else "disabled"
        )

        self.last_btn.config(
            state="normal"
            if self.current_page < total_pages - 1
            else "disabled"
        )

    def create_product_row(self, product):
        row = tk.Frame(
            self.products_frame,
            bd=1,
            relief="solid",
            padx=10,
            pady=6
        )
        row.pack(fill="x", padx=5, pady=2)

        label_text = (
            f"{product['name']} | "
            f"{product['price']} kr | "
            f"ID: {product['id']} | "
            f"{product['type']}"
        )

        tk.Label(
            row,
            text=label_text,
            anchor="w",
            justify="left"
        ).pack(side="left", fill="x", expand=True)

        tk.Button(
            row,
            text="Open Product",
            command=lambda p=product: webbrowser.open(
                self.build_link(p)
            )
        ).pack(side="right", padx=5)

        tk.Button(
            row,
            text="Delete",
            bg="red",
            fg="white",
            command=lambda p=product: self.delete_product(p)
        ).pack(side="right", padx=5)

    def delete_product(self, product):
        if not messagebox.askyesno(
            "Delete Product",
            f"Delete '{product['name']}'?"
        ):
            return

        self.products.remove(product)
        self.save_json()

        total_pages = max(
            1,
            (len(self.products) + ITEMS_PER_PAGE - 1) // ITEMS_PER_PAGE
        )

        if self.current_page >= total_pages:
            self.current_page = total_pages - 1

        self.refresh_page()

    def previous_page(self):
        if self.current_page > 0:
            self.current_page -= 1
            self.refresh_page()

    def next_page(self):
        total_pages = max(
            1,
            (len(self.products) + ITEMS_PER_PAGE - 1) // ITEMS_PER_PAGE
        )

        if self.current_page < total_pages - 1:
            self.current_page += 1
            self.refresh_page()

    def last_page(self):
        total_pages = max(
            1,
            (len(self.products) + ITEMS_PER_PAGE - 1) // ITEMS_PER_PAGE
        )

        self.current_page = total_pages - 1
        self.refresh_page()


if __name__ == "__main__":
    root = tk.Tk()
    app = ProductViewer(root)
    root.mainloop()