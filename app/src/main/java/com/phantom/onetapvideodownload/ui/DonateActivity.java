package com.phantom.onetapvideodownload.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.phantom.onetapvideodownload.R;
import com.phantom.onetapvideodownload.utils.CheckPreferences;

import java.util.ArrayList;
import java.util.List;

public class DonateActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {
    private BillingProcessor mBillingProcessor;
    private String mPublicLicenseKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkRWe3yER6u5+R4OWEn2DfIsdM7Ojxm+gVTbQrLJpROg+qr4zqcN/SKJB+yi9Jos8BvAHPemx7OX9uTjDGoBJ//GyScNGN+IQUKdXOUrrPtZGqeR02QlnonF5dM/abIKwlEX4qiIERYtsooi87k4kPn1cCe55YE8wyZkRHR5vy3rjJ0BMLkVkTVVlxlRgy+h0ihbDVvDU2sbb3kEDc5mOW5n6+hoofhCPoErzUhSlTOCmDxL1AaISA05JqOvWliSAQoM9ixCaWMoHcbnzf8HyR7ijyUNdifn6R9a791lvk2b8Ry5Y96p+VNsnLNUNrCjwwXkuJnmBvntRFrtrr0I0TwIDAQAB";
    private String TAG = "DonateActivity";
    private String donationProductId = "donate";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (CheckPreferences.getDonationStatus(this)) {
            setContentView(R.layout.activity_donated);
        } else {
            setContentView(R.layout.activity_not_donate);
        }

        mBillingProcessor = new BillingProcessor(this, mPublicLicenseKey, this);

        Button donateButton = (Button)findViewById(R.id.donate_button);
        donateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<String> donationMethods = new ArrayList<>();
                donationMethods.add(v.getContext().getString(R.string.via_wallet));
                donationMethods.add(v.getContext().getString(R.string.via_playstore));

                new MaterialDialog.Builder(v.getContext())
                        .items(donationMethods)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                switch(which) {
                                    case 0 :
                                        String url = "https://wallet.google.com/manage/#sendMoney:bansal.ashish096@gmail.com";
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setData(Uri.parse(url));
                                        startActivity(intent);
                                        break;
                                    case 1 :
                                        boolean isAvailable = BillingProcessor.isIabServiceAvailable(DonateActivity.this);
                                        if(isAvailable) {
                                            mBillingProcessor.purchase(DonateActivity.this, donationProductId);
                                        } else {
                                            new MaterialDialog.Builder(dialog.getContext())
                                                    .title(R.string.google_services_not_found_title)
                                                    .content(R.string.google_services_not_found_summary)
                                                    .positiveText(R.string.okay)
                                                    .show();
                                        }
                                        break;
                                }
                            }
                        })
                        .show();
            }
        });
    }

    @Override
    public void onBillingInitialized() {
        /*
         * Called when BillingProcessor was initialized and it's ready to purchase
         */
        Log.e(TAG, "Billing Processor Initialized");
        if (!CheckPreferences.getDonationStatus(this)) {
            mBillingProcessor.loadOwnedPurchasesFromGoogle();
        }
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        /*
         * Called when requested PRODUCT ID was successfully purchased
         */

        if (details != null) {
            CheckPreferences.setDonationStatus(this, true);
            setContentView(R.layout.activity_donated);
        }
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        /*
         * Called when some error occurred. See Constants class for more details
         */
    }

    @Override
    public void onPurchaseHistoryRestored() {
        /*
         * Called when purchase history was restored and the list of all owned PRODUCT ID's
         * was loaded from Google Play
         */

        TransactionDetails transactionDetails = mBillingProcessor.getPurchaseTransactionDetails(donationProductId);
        if (transactionDetails != null) {
            CheckPreferences.setDonationStatus(this, true);
            setContentView(R.layout.activity_donated);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mBillingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        if (mBillingProcessor != null)
            mBillingProcessor.release();

        super.onDestroy();
    }
}
