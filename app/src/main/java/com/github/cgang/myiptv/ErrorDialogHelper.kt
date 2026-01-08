package com.github.cgang.myiptv

import android.app.AlertDialog
import android.content.Context
import android.text.Html
import android.text.Spanned
import android.widget.TextView
import android.widget.ScrollView
import android.widget.LinearLayout
import android.widget.Button
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.graphics.Color
import androidx.core.widget.NestedScrollView

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
        val dialogView = createDialogView(context, userMessage, technicalDetails)
        
        val builder = AlertDialog.Builder(context)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok, null)
        
        val dialog = builder.show()
        
        // Set up the details button
        val detailsButton = dialogView.findViewById<Button>(R.id.details_button)
        val technicalDetailsView = dialogView.findViewById<TextView>(R.id.technical_details)
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
    
    private fun createDialogView(context: Context, userMessage: String, technicalDetails: String): View {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 30)
        }
        
        // User-friendly message
        val userMessageView = TextView(context).apply {
            id = R.id.user_message
            text = userMessage
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(getThemeColor(context, android.R.attr.textColorPrimary))
        }
        
        layout.addView(userMessageView)
        
        // Separator
        val separator = View(context).apply {
            id = R.id.separator
            setBackgroundColor(Color.GRAY)
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                2
            ).apply {
                setMargins(0, 30, 0, 30)
            }
        }
        
        layout.addView(separator)
        
        // Technical details (initially hidden)
        val technicalDetailsView = TextView(context).apply {
            id = R.id.technical_details
            text = technicalDetails
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTextColor(getThemeColor(context, android.R.attr.textColorSecondary))
            visibility = View.GONE
            typeface = android.graphics.Typeface.MONOSPACE
        }
        
        // Wrap technical details in a scrollable container
        val scrollView = NestedScrollView(context).apply {
            id = R.id.scroll_view
            addView(technicalDetailsView)
            visibility = View.GONE
        }
        
        layout.addView(scrollView)
        
        // Details button
        val detailsButton = Button(context).apply {
            id = R.id.details_button
            text = context.getString(R.string.show_details)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 20, 0, 0)
            }
        }
        
        layout.addView(detailsButton)
        
        return layout
    }
    
    private fun getThemeColor(context: Context, attr: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }
}