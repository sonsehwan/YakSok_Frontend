package com.example.medication.ui.friend;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class PortraitCaptureActivity extends CaptureActivity {

    @Override
    protected DecoratedBarcodeView initializeContent() {
        DecoratedBarcodeView view = super.initializeContent();
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.getStatusView().setPadding(0, 0, 0, bars.bottom);
            return insets;
        });
        return view;
    }
}
