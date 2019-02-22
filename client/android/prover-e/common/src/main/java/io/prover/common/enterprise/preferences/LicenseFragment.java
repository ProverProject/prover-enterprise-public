package io.prover.common.enterprise.preferences;


import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.nayuki.qrcodegen.QrCode;
import io.prover.common.R;
import io.prover.common.enterprise.transport.ProverEnterpriseTransport;
import io.prover.common.enterprise.transport.response.ServerStatus;
import io.prover.common.transport.base.INetworkRequestBasicListener;
import io.prover.common.transport.base.NetworkRequest;
import io.prover.common.util.Util;

import static io.prover.common.Const.ENTERPRISE_SERVER_URI;

/**
 * A simple {@link Fragment} subclass.
 */
public class LicenseFragment extends Fragment implements INetworkRequestBasicListener<ServerStatus> {

    private ViewGroup root;
    private ImageView qrCodeViewView;
    private TextView addressTextView;
    private View licenseLogoView;

    public LicenseFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = (ViewGroup) inflater.inflate(R.layout.fragment_license, container, false);
        qrCodeViewView = root.findViewById(R.id.qrCodeView);
        addressTextView = root.findViewById(R.id.licenseNumberText);
        licenseLogoView = root.findViewById(R.id.licenseLogoView);

        String uri = PreferenceManager.getDefaultSharedPreferences(inflater.getContext())
                .getString(ENTERPRISE_SERVER_URI, "");
        ProverEnterpriseTransport.getInstance().getServerStatus(Uri.parse(uri), this);
        addressTextView.setVisibility(View.GONE);
        addressTextView.setOnClickListener(v -> {
            String address = addressTextView.getText().toString();
            Util.copyToClipboard(v.getContext(), address);
            Toast.makeText(v.getContext(), R.string.license_number_copied, Toast.LENGTH_SHORT).show();
        });
        return root;
    }

    @Override
    public void onNetworkRequestDone(NetworkRequest request, ServerStatus responce) {
        QrCode code = QrCode.encodeText(responce.address, QrCode.Ecc.QUARTILE);

        DisplayMetrics dm = root.getResources().getDisplayMetrics();
        int scrSize = Math.min(dm.widthPixels, dm.heightPixels);
        int border = 0;
        int scale = (scrSize - qrCodeViewView.getPaddingLeft() - qrCodeViewView.getPaddingRight()) / (code.size + border * 2);

        Bitmap bitmap = code.toImage(scale, border, Bitmap.Config.ARGB_4444, 0xFF3b3d47, 0x00000000);
        qrCodeViewView.setImageBitmap(bitmap);
        addressTextView.setText(responce.address);
        licenseLogoView.setVisibility(View.VISIBLE);
        addressTextView.setVisibility(View.VISIBLE);
    }
}
