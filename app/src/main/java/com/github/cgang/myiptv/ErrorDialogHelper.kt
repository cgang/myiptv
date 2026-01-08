package com.github.cgang.myiptv

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.view.View
import android.widget.LinearLayout

/**
 * A helper class to create user-friendly error dialogs that show simple messages
 * by default but allow developers to see detailed error information when needed.
 */
object ErrorDialogHelper {

    /**
     * Shows an error dialog with a user-friendly message and optional technical details
     *
     * @param context The context to use
     * @param title The dialog title
     * @param userMessage A simple, user-friendly message
     * @param technicalDetails Technical error details for developers
     */
    fun showErrorDialog(
        context: Context,
        title: String,
        userMessage: String,
        technicalDetails: String
    ) {
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.error_dialog, null)

        // Set the user message
        val userMessageView = dialogView.findViewById<TextView>(R.id.user_message)
        userMessageView.text = userMessage

        // Set the technical details
        val technicalDetailsView = dialogView.findViewById<TextView>(R.id.technical_details)
        technicalDetailsView.text = technicalDetails

        val builder = AlertDialog.Builder(context)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok, null)

        val dialog = builder.show()

        // Set up the details button
        val detailsButton = dialogView.findViewById<Button>(R.id.details_button)
        val separator = dialogView.findViewById<View>(R.id.separator)

        detailsButton.setOnClickListener {
            if (technicalDetailsView.visibility == View.GONE) {
                technicalDetailsView.visibility = View.VISIBLE
                separator.visibility = View.VISIBLE
                detailsButton.text = context.getString(R.string.hide_details)
            } else {
                technicalDetailsView.visibility = View.GONE
                separator.visibility = View.GONE
                detailsButton.text = context.getString(R.string.show_details)
            }
        }
    }
}