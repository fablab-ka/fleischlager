package de.fablabka.apps.fleischlager;

import java.util.ArrayList;

public interface InventoryProvider {
    public ArrayList<InventoryManager.Product> getProducts();
}
