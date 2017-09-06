package com.dantann.backupexample

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.drive.*
import timber.log.Timber
import java.io.BufferedOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*


class GoogleBackUpFragment : Fragment() {

    private var isCreating: Boolean = false
    private var progressDialog: ProgressDialog? = null

    private var filesArrayAdapter: ArrayAdapter<FileData>? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_google_backup, container, false)
        val button = view.findViewById<Button>(R.id.addFileButton)
        button.setOnClickListener{view -> onCreateFileButtonClick(view)}
        return view
    }

    override fun onResume() {
        super.onResume()
        if (filesArrayAdapter == null) {
            filesArrayAdapter = object : ArrayAdapter<FileData>(activity,
                    R.layout.file_list_item) {

                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val inflater = LayoutInflater.from(context)
                    val itemView = inflater.inflate(R.layout.file_list_item, parent, false)
                    val fileNameView = itemView.findViewById<TextView>(R.id.file_name)
                    val fileName = getItem(position)!!.title
                    fileNameView.text = fileName
                    val fileSize = itemView.findViewById<TextView>(R.id.file_size)
                    val fileSizeInBytes = NumberFormat.getInstance()
                            .format(getItem(position)!!.fileSize)
                    fileSize.text = fileSizeInBytes
                    return itemView
                }
            }
            val filesListView = view!!.findViewById<ListView>(R.id.file_list)
            filesListView.adapter = filesArrayAdapter
            updateListOfFiles()
        }
    }

    fun getGoogleApiClient() : GoogleApiClient {
        return (activity as BaseGoogleActivity).getGoogleApiClient()!!
    }

    fun showMessage(msg : String) {
        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
    }

    /**
     * A handler function for a Create File button click event.
     *
     * @param view a reference to the Create File button view.
     */
    fun onCreateFileButtonClick(view: View) {
        if (isCreating) return

        isCreating = true
        showProgressDialog()
        Drive.DriveApi.newDriveContents(getGoogleApiClient())
                .setResultCallback(DriveContentCallback())
    }

    private fun showProgressDialog() {
        if (progressDialog != null) return

        progressDialog = ProgressDialog(activity)
        progressDialog!!.setMessage("Creating Spreadsheet.... ")
        progressDialog!!.setIndeterminate(true)
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setCanceledOnTouchOutside(false)
        progressDialog!!.show()
    }

    inner class DriveContentCallback : ResultCallback<DriveApi.DriveContentsResult> {

        override fun onResult(result: DriveApi.DriveContentsResult) {
            if (!result.status.isSuccess) {
                showMessage("Error while trying to create new file contents")
                return
            }

            try {
                val driveContents = result.driveContents
                val out = driveContents.outputStream

                var bufOut: BufferedOutputStream? = null
                val size = 1000 //bytes

                bufOut = BufferedOutputStream(out)
                for (i in 0..size - 1) {
                    val b = (255 * Math.random()).toByte()
                    bufOut.write(b.toInt())
                }

                val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
                val currentDateandTime = sdf.format(Date())

                val changeSet = MetadataChangeSet.Builder()
                        .setTitle("TEST_FILE_" + currentDateandTime)
                        .setMimeType("text/plain")
                        .setStarred(true).build()

                val executionOptions = ExecutionOptions.Builder()
                        .setNotifyOnCompletion(true)
                        .build()

                // create a file on root folder
                Drive.DriveApi.getAppFolder(getGoogleApiClient())
                        .createFile(
                                getGoogleApiClient(),
                                changeSet,
                                driveContents,
                                executionOptions)
                        .setResultCallback(FileCallback())

            } catch (ex: Exception) {
                Timber.e(ex)
            }

        }
    }

    private inner class FileCallback : ResultCallback<DriveFolder.DriveFileResult> {

        override fun onResult(result: DriveFolder.DriveFileResult) {
            isCreating = false
            if (!result.status.isSuccess) {
                showMessage("Error while trying to create the file")
                return
            }
            if (progressDialog != null) {
                progressDialog!!.dismiss()
                progressDialog = null
            }
            showMessage("Created a file with content: " + result.driveFile.driveId)
            updateListOfFiles()
        }
    }


    fun updateListOfFiles() {
        if (getGoogleApiClient().isConnected) {
            Drive.DriveApi.getAppFolder(getGoogleApiClient()).listChildren(getGoogleApiClient()).setResultCallback{
                result -> onResult(result)
            }
        } else {
            Timber.e("Not connected to google api")
        }
    }

    fun onResult(result : DriveApi.MetadataBufferResult) {
        if (!result.status.isSuccess) {
            showMessage("Problem while retrieving results")
            return
        }
        if (filesArrayAdapter!!.count > 0) filesArrayAdapter!!.clear()
        var mdb: MetadataBuffer? = null
        try {
            mdb = result.metadataBuffer
            for (md in mdb!!) {
                if (md == null || !md.isDataValid || md.isTrashed) continue
                Timber.d("found ${md.title}")
                filesArrayAdapter!!.add(FileData(md.title, md.fileSize))
            }
        } finally {
            if (mdb != null) mdb.close()
        }
    }

    data class FileData(
            val title: String,
            val fileSize : Long)

}