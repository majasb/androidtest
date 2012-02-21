package bratseth.maja.androidtest.client;

import static bratseth.maja.androidtest.client.StopWatch.*;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import bratseth.maja.androidtest.service.*;
import bratseth.maja.androidtest.service.aidl.AidlCustomer;
import bratseth.maja.androidtest.service.aidl.AidlCustomerId;
import bratseth.maja.androidtest.service.aidl.AidlCustomerService;
import bratseth.maja.androidtest.spi.Customer;
import bratseth.maja.androidtest.spi.CustomerEvent;
import bratseth.maja.androidtest.spi.CustomerId;
import bratseth.maja.androidtest.spi.CustomerService;

public class ClientActivity extends Activity {

    private ServiceLocator serviceLocator;
    private CustomerService customerService;
    private TextView customerView;
    private Button button;
    private Button errorButton;
    private Button eventButton;
    private Button benchButton;
    private Button aidlBenchButton;
    private ClientEventListener listener;
    private AidlCustomerService aidlService;
    private int n = 10;
    private Button latencyJavaButton;
    private Button latencyAidlButton;

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
        
        eventButton = new Button(this);
        eventButton.setText("Publish customer event");
        main.addView(eventButton);

        benchButton = new Button(this);
        benchButton.setText("Benchmark customer pull (java serialization)");
        main.addView(benchButton);

        aidlBenchButton = new Button(this);
        aidlBenchButton.setText("Benchmark customer pull (aidl)");
        main.addView(aidlBenchButton);

        latencyJavaButton = new Button(this);
        latencyJavaButton.setText("Latency (java serialization)");
        main.addView(latencyJavaButton);

        latencyAidlButton = new Button(this);
        latencyAidlButton.setText("Latency (aidl)");
        main.addView(latencyAidlButton);

        customerView = new TextView(this);
        customerView.setText("No customer yet");
        main.addView(customerView);
        setContentView(main);

        final ServiceConnection connection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                serviceLocator = new ClientServiceLocator(TransportService.Stub.asInterface(service));
                serviceLocator.addEventListener(listener);
                customerService = serviceLocator.locate(CustomerService.class);
            }
            public void onServiceDisconnected(ComponentName className) {
                finish();
            }
        };
        ServiceConnection plainConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                aidlService = AidlCustomerService.Stub.asInterface(iBinder);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };
        bindService(new Intent(TransportService.class.getName()), connection, Context.BIND_AUTO_CREATE);
        bindService(new Intent("bratseth.maja.androidtest.service.PlainService"), plainConnection, Context.BIND_AUTO_CREATE);
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
        eventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CustomerTrigger().execute();
            }
        });
        benchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomerId id = new CustomerId("1");
                StopWatch watch = new StopWatch();
                watch.start();
                for (int i=0; i< n; i++) {
                    Customer customer = customerService.getCustomer(id);
                }
                Duration duration = watch.stop();
                Log.d("benching", "Serializable: Took " + duration.toMillies() + "ms to call getCustomer " + n + " times");
            }
        });
        aidlBenchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AidlCustomerId id = new AidlCustomerId("1");
                StopWatch watch = new StopWatch();
                watch.start();
                for (int i=0; i< n; i++) {
                    try {
                        AidlCustomer customer = aidlService.getCustomer(id);
                    } catch (RemoteException e) {
                        Log.e("benching", "Error calling aidl service", e);
                    }
                }
                Duration duration = watch.stop();
                Log.d("benching", "AIDL: Took " + duration.toMillies() + "ms to call getCustomer " + n + " times");
            }
        });
        latencyJavaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomerId id = new CustomerId("1");
                StopWatch watch = new StopWatch();
                watch.start();
                long endTime = customerService.latencyTest(id);
                Duration duration = watch.stop(endTime);
                Log.d("benching", "Latency Java Serialization: " + duration.pretty() + " on method with one CustomerId parameter");
            }
        });
        latencyAidlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AidlCustomerId id = new AidlCustomerId("1");
                StopWatch watch = new StopWatch();
                watch.start();
                try {
                    long endTime = aidlService.latencyTest(id);
                    Duration duration = watch.stop(endTime);
                    Log.d("benching", "Latency Aidl: " + duration.pretty() + " on method with one AidlCustomerId parameter");
                } catch (RemoteException e) {
                    Log.e("benching", "Error calling method", e);
                }
            }
        });

        listener = new CustomerEventListener();
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

    private class CustomerTrigger extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            customerService.publishSomething();
            return null;
        }
    }

    private class CustomerEventListener implements ClientEventListener {
        @Override
        public void notify(Object e) {
            if (e instanceof CustomerEvent) {
                CustomerEvent event = (CustomerEvent) e;
                Log.d("cisco", "Got customer event " + event);
            }
        }
    }
}
