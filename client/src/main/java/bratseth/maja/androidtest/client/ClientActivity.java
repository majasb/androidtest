package bratseth.maja.androidtest.client;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import bratseth.maja.androidtest.service.*;
import bratseth.maja.androidtest.spi.Customer;
import bratseth.maja.androidtest.spi.CustomerId;
import bratseth.maja.androidtest.spi.CustomerService;

public class ClientActivity extends Activity {

    private ServiceLocator serviceLocator;
    private CustomerService customerService;
    private TextView customerView;
    private Button button;
    private Button errorButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ViewGroup main = (ViewGroup) View.inflate(this, R.layout.main, null);
        
        button = new Button(this);
        button.setText("Load customer");
        main.addView(button);
        
        errorButton = new Button(this);
        errorButton.setText("Load customer with error");
        main.addView(errorButton);
        
        customerView = new TextView(this);
        customerView.setText("No customer yet");
        main.addView(customerView);
        setContentView(main);

        final ServiceConnection connection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                serviceLocator = new ClientServiceLocator(TransportService.Stub.asInterface(service));
                customerService = serviceLocator.locate(CustomerService.class);
            }
            public void onServiceDisconnected(ComponentName className) {
                finish();
            }
        };
        bindService(new Intent(TransportService.class.getName()), connection, Context.BIND_AUTO_CREATE);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CustomerLoader().execute(new CustomerId("1"));
            }
        });
        errorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CustomerLoader().execute(new CustomerId("2"));
            }
        });
    }

    private class CustomerLoader extends AsyncTask<CustomerId, Void, Customer> {
        private volatile Throwable exception;

        protected Customer doInBackground(CustomerId... customerIds) {
            try {
                return customerService.getCustomer(customerIds[0]);
            } catch (Throwable e) {
                this.exception = e;
                Log.e(ClientActivity.class.getSimpleName(), "Error!", e);
                return null;
            }
        }

        protected void onPostExecute(Customer customer) {
            if (exception != null) {
                Toast.makeText(ClientActivity.this, "Exception: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            } else {
                customerView.setText(customer.getName());
                Log.i(ClientActivity.class.getSimpleName(), "Got customer");
            }
        }
    }

}
