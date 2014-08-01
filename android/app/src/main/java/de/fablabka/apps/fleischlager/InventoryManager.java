package de.fablabka.apps.fleischlager;

import com.debortoliwines.openerp.api.FilterCollection;
import com.debortoliwines.openerp.api.ObjectAdapter;
import com.debortoliwines.openerp.api.Row;
import com.debortoliwines.openerp.api.RowCollection;
import com.debortoliwines.openerp.api.Session;

import java.util.ArrayList;
import java.util.List;

public class InventoryManager {
    private final String hostname;
    private final String[] relevantProductFields;
    private final String[] relevantUserFields;
    private String dbname;
    private String username;
    private String password;
    private Boolean loggedIn;

    public InventoryManager(String dbname, String hostname) {
        this.dbname = dbname;
        this.hostname = hostname;

        this.relevantProductFields = new String[] { "name", "default_code", "type", "qty_available", "loc_case", "loc_rack", "loc_row", "ean13", "list_price" };
        this.relevantUserFields = new String[] { "name" };
    }

    public void Login(String username, String password) {
        this.username = username;
        this.password = password;

        Session openERPSession = new Session(this.hostname, 80, this.dbname, this.username, this.password);
        try {
            // startSession logs into the server and keeps the user id of the logged in user
            openERPSession.startSession();

            System.out.println("OpenERP Version: " + openERPSession.getServerVersion());
        } catch (Exception e) {
            System.out.println("Error while reading data from server:\n\n" + e.getMessage());
        }

        this.loggedIn = true;
    }

    public List<Product> SearchProducts(String name)
    {
        ArrayList<Product> result = new ArrayList<Product>();

        Session openERPSession = new Session(this.hostname, 80, this.dbname, this.username, this.password);
        try {
            // startSession logs into the server and keeps the user id of the logged in user
            openERPSession.startSession();
            ObjectAdapter productAdapter = openERPSession.getObjectAdapter("product.product");

            FilterCollection filters = new FilterCollection();
            filters.add("name", "=", name);
            RowCollection products = productAdapter.searchAndReadObject(filters, this.relevantProductFields);

            for (Row row : products){
                result.add(new Product(row));
            }
        } catch (Exception e) {
            System.out.println("Error while reading data from server:\n\n" + e.getMessage());
        }

        return result;
    }

    public ArrayList<Product> GetProducts()
    {
        ArrayList<Product> result = new ArrayList<Product>();

        Session openERPSession = new Session(this.hostname, 80, this.dbname, this.username, this.password);
        try {
            // startSession logs into the server and keeps the user id of the logged in user
            openERPSession.startSession();
            ObjectAdapter productAdapter = openERPSession.getObjectAdapter("product.product");

            RowCollection products = productAdapter.searchAndReadObject(new FilterCollection(), this.relevantProductFields);

            for (Row row : products){
                result.add(new Product(row));
            }
        } catch (Exception e) {
            System.out.println("Error while reading data from server:\n\n" + e.getMessage());
        }

        return result;
    }

    public Product GetProduct(String id) {
        Product result = null;

        Session openERPSession = new Session(this.hostname, 80, this.dbname, this.username, this.password);
        try {
            // startSession logs into the server and keeps the user id of the logged in user
            openERPSession.startSession();
            ObjectAdapter productAdapter = openERPSession.getObjectAdapter("product.product");

            RowCollection products = productAdapter.readObject(new String[]{id}, this.relevantProductFields);

            if (products.size() > 0) {
                result = new Product(products.get(0));
            }
        } catch (Exception e) {
            System.out.println("Error while reading data from server:\n\n" + e.getMessage());
        }

        return result;
    }

    public class Product
    {
        private final int id;
        private final String name;
        private final String default_code;
        private final String type;
        private final String qty_available;
        private final String loc_case;
        private final String loc_rack;
        private final String loc_row;
        private final String ean13;
        private final String list_price;

        public Product(Row row)
        {
            this.id = row.getID();
            this.name = row.get("name").toString();
            this.default_code = row.get("default_code").toString();
            this.type = row.get("type").toString();
            this.qty_available = row.get("qty_available").toString();
            this.loc_case = row.get("loc_case").toString();
            this.loc_rack = row.get("loc_rack").toString();
            this.loc_row = row.get("loc_row").toString();
            this.ean13 = row.get("ean13").toString();
            this.list_price = row.get("list_price").toString();
        }

        public String getList_price() {
            return list_price;
        }

        public String getEan13() {
            return ean13;
        }

        public String getLoc_row() {
            return loc_row;
        }

        public String getLoc_rack() {
            return loc_rack;
        }

        public String getLoc_case() {
            return loc_case;
        }

        public String getQty_available() {
            return qty_available;
        }

        public String getType() {
            return type;
        }

        public String getDefault_code() {
            return default_code;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }
    }

    public class User
    {
        public User(Row row)
        {

        }
    }
}
