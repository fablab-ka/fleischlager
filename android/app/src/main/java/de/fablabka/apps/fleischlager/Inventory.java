package de.fablabka.apps.fleischlager;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.ndeftools.Record;
import org.ndeftools.externaltype.AndroidApplicationRecord;
import org.ndeftools.wellknown.TextRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Inventory extends Activity {

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NFC Tag";

    private NfcAdapter mNfcAdapter;
    private PendingIntent nfcPendingIntent;

    private TextView stateLabel;
    private ProgressBar progressBar;
    private ListView listView;
    private static InventoryManager inventoryManager;
    private ArrayList<InventoryManager.Product> products;
    private StableArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        stateLabel = (TextView)findViewById(R.id.state_label);
        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        listView = (ListView)findViewById(R.id.listview_products);

        this.inventoryManager = new InventoryManager("FabLab_Kalsruhe", getResources().getString(R.string.erp_hostname));
        this.inventoryManager.Login("fleischlager", "fleischlager");
        progressBar.setVisibility(View.VISIBLE);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        if (mNfcAdapter == null) {
            Toast.makeText(this, R.string.error_nfc_not_supported, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (!mNfcAdapter.isEnabled()) {
            stateLabel.setText(R.string.state_nfc_disabled);
        } else {
            //stateLabel.setText(R.string.state_nothing_selected);
        }

        adapter = new StableArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, new ArrayList<InventoryManager.Product>());
        this.listView.setAdapter(adapter);
        this.listView.setVisibility(View.INVISIBLE);

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(Inventory.this.getApplicationContext(), "Click ListItem Number " + position, Toast.LENGTH_LONG).show();
            }
        });

        handleIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();

        setupForegroundDispatch();
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopForegroundDispatch();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");

        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.inventory, menu);

        SearchView searchView = (SearchView)menu.findItem(R.id.action_search).getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onSearchRequested() {

        return super.onSearchRequested();
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_SEARCH.equals(action)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            searchProduct(query);
        } else if (intent.hasExtra(NfcAdapter.EXTRA_TAG)) {

            Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (messages != null) {
                vibrate();

                NdefMessage ndefMessage = (NdefMessage)messages[0];

                try {
                    NdefRecord[] records = ndefMessage.getRecords();

                    for(int k = 0; k < records.length; k++) {
                        Record record = Record.parse(records[k]);

                        Log.d(TAG, " Record #" + k + " is of class " + record.getClass().getName());

                        if(record instanceof AndroidApplicationRecord) {
                            AndroidApplicationRecord aar = (AndroidApplicationRecord)record;
                            Log.d(TAG, "Package is " + aar.getPackageName());

                            if (aar.getPackageName() != "de.fablabka.apps.fleischlager") {
                                stateLabel.setText(R.string.state_wrong_tag_type);
                            }
                        } else if (record instanceof TextRecord) {
                            TextRecord tr = (TextRecord)record;
                            Log.d(TAG, "Content is " + tr.getText());
                            stateLabel.setText("product id: " + tr.getText());
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Problem parsing message", e);
                }
            }
        } else {
            new LoadProductsTask().execute();
        }
    }

    private void searchProduct(String query) {

    }

    private void vibrate() {
        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);
    }

    public void setupForegroundDispatch() {
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter[] writeTagFilters = new IntentFilter[] {tagDetected};

        mNfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, writeTagFilters, null);
    }

    public void stopForegroundDispatch() {
        mNfcAdapter.disableForegroundDispatch(this);
    }

    public class LoadProductsTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressBar.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            final ArrayList<InventoryManager.Product> products = inventoryManager.GetProducts();

            runOnUiThread(new Runnable() {
                public void run() {
                    adapter.clear();

                    if (products.size() > 0)
                    {
                        for (InventoryManager.Product p : products) {
                            if (p.getType().equals("product")) {
                                adapter.add(p);
                            }
                        }
                    } else
                    {
                        stateLabel.setText(R.string.no_products_found);
                    }
                }
            });

            return true;
        }
    }

    private class StableArrayAdapter extends ArrayAdapter<InventoryManager.Product> {
        private LayoutInflater inflater = null;

        public StableArrayAdapter(Context context, int textViewResourceId, List<InventoryManager.Product> objects) {
            super(context, textViewResourceId, objects);
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public long getItemId(int position) {
            InventoryManager.Product item = getItem(position);
            return item.getId();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View vi=convertView;
            if(convertView == null) {
                vi = inflater.inflate(R.layout.product_list_item, null);
            }

            TextView title = (TextView)vi.findViewById(R.id.product_row_title);
            TextView description = (TextView)vi.findViewById(R.id.product_row_description);
            TextView location = (TextView)vi.findViewById(R.id.product_row_location);
            ImageView icon = (ImageView)vi.findViewById(R.id.product_row_icon);

            InventoryManager.Product item = getItem(position);

            String name = item.getName();
            title.setText(name);
            description.setText(item.getDefault_code());
            //location.setText(item.getLocation());

            //decide icon by item.getType()
            //TODO icon.setImage();
            return vi;
        }
    }
}

