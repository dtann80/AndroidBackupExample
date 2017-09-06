package com.dantann.backupexample


import android.os.Bundle
import com.google.android.gms.drive.Drive
import timber.log.Timber


class GoogleBackUpActivity : BaseGoogleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_backup)
    }

    override fun onConnected(connectionHint: Bundle?) {
        super.onConnected(connectionHint)
        if (getGoogleApiClient()!!.isConnected) {
            Drive.DriveApi.requestSync(getGoogleApiClient()).setResultCallback { status ->
                if (!status.status.isSuccess) {
                    Timber.d("SYNCING ERROR" + status.statusMessage!!)
                } else {
                    Timber.d("SYNCING SUCCESS")
                    addFragmentIfNeeded()
                }
            }
        }
    }

    fun addFragmentIfNeeded() {
        val TAG = "FRAG_TAG"
        var fragment = supportFragmentManager.findFragmentByTag(TAG)

        if (fragment == null) {
            fragment = GoogleBackUpFragment()
            supportFragmentManager.beginTransaction()
                    .add(R.id.container, fragment, TAG)
                    .commit()
        }
    }
}