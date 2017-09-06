package com.dantann.backupexample

import android.app.Activity
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.drive.Drive
import timber.log.Timber


open class BaseGoogleActivity : AppCompatActivity(),
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private val TAG = BaseGoogleActivity::class.java.getSimpleName()

    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected val REQUEST_CODE_RESOLUTION = 1

    /**
     * Next available request code.
     */
    protected val NEXT_AVAILABLE_REQUEST_CODE = 2

    /**
     * Google API client.
     */
    private var mGoogleApiClient: GoogleApiClient? = null

    /**
     * Called when activity gets visible. A connection to Drive services need to
     * be initiated as soon as the activity is visible. Registers
     * `ConnectionCallbacks` and `OnConnectionFailedListener` on the
     * activities itself.
     */
    override fun onResume() {
        super.onResume()
        if (mGoogleApiClient == null) {
            mGoogleApiClient = GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_APPFOLDER) // required for App Folder sample
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build()
        }
        mGoogleApiClient!!.connect()
    }

    /**
     * Handles resolution callbacks.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int,
                                  data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("CALLED")
        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == Activity.RESULT_OK) {
            mGoogleApiClient!!.connect()
        }
    }

    /**
     * Called when activity gets invisible. Connection to Drive service needs to
     * be disconnected as soon as an activity is invisible.
     */
    override fun onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient!!.disconnect()
        }
        super.onPause()
    }

    /**
     * Called when `mGoogleApiClient` is connected.
     */
    override fun onConnected(connectionHint: Bundle?) {
        Log.i(TAG, "GoogleApiClient connected " + connectionHint)
    }

    /**
     * Called when `mGoogleApiClient` is disconnected.
     */
    override fun onConnectionSuspended(cause: Int) {
        Log.i(TAG, "GoogleApiClient connection suspended")
    }

    /**
     * Called when `mGoogleApiClient` is trying to connect but failed.
     * Handle `result.getResolution()` if there is a resolution is
     * available.
     */
    override fun onConnectionFailed(result: ConnectionResult) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString())
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.errorCode, 0).show()
            return
        }
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION)
        } catch (e: SendIntentException) {
            Log.e(TAG, "Exception while starting resolution activity", e)
        }

    }

    /**
     * Shows a toast message.
     */
    fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Getter for the `GoogleApiClient`.
     */
    fun getGoogleApiClient(): GoogleApiClient? {
        return mGoogleApiClient
    }
}