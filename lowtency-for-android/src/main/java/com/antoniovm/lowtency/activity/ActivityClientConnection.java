package com.antoniovm.lowtency.activity;

import java.net.URI;
import java.net.URISyntaxException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.antoniovm.lowtency.R;

public class ActivityClientConnection extends Activity {
	
	private String ip;
	private int port;
	private Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_client_connection);

		init();

	}

	/**
	 * 
	 */
	private void init() {

		initData();
		initViews();

	}

	/**
	 * 
	 */
	private void initViews() {

		Button bQRConnection = (Button) findViewById(R.id.bConnectQR);

		bQRConnection.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent("com.google.zxing.client.android.SCAN");
				intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
				startActivityForResult(intent, 0);
			}
		});

		Button bConnect = (Button) findViewById(R.id.bConnect);

		bConnect.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(ActivityClientConnection.this, ActivityClientStreaming.class));
				finish();
			}
		});

	}

	/**
	 * 
	 */
	private void initData() {
		this.intent = new Intent(this, ActivityClientStreaming.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		startActivity(new Intent(ActivityClientConnection.this, ActivityMain.class));
		finish();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	


	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			switch (resultCode) {
			case RESULT_OK:
				String out = intent.getStringExtra("SCAN_RESULT");
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				// Validate input qr
				if (isValidURI(out)) {
					startClientStreamActivity();
				}
				break;
			case RESULT_CANCELED:
			default:
				break;
			}
		}
	}

	/**
	 * 
	 */
	private boolean isValidURI(String input) {
		URI uri = null;
		try {
			uri = new URI(input);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		String lLowtency = getString(R.string.l_lowtency);

		if (!uri.getScheme().equals(lLowtency)) {
			return false;
		}

		ip = uri.getHost();
		port = uri.getPort();

		return true;

	}

	/**
	 * 
	 */
	private void startClientStreamActivity() {
		intent.putExtra("IP", ip);
		intent.putExtra("PORT", port);
		startActivity(intent);
		finish();
	}

}
