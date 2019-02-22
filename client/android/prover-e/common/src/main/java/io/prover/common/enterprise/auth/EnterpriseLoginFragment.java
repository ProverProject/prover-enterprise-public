package io.prover.common.enterprise.auth;

import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import io.prover.common.R;
import io.prover.common.enterprise.transport.ProverEnterpriseTransport;
import io.prover.common.enterprise.transport.response.ServerStatus;
import io.prover.common.pages.base.PageFragment;
import io.prover.common.transport.base.INetworkRequestListener;
import io.prover.common.transport.base.NetworkRequest;
import io.prover.common.util.TextInputLayoutErrorWatcher;

import static android.support.design.widget.Snackbar.LENGTH_SHORT;
import static io.prover.common.Const.ENTERPRISE_SERVER_URI;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AuthFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class EnterpriseLoginFragment extends PageFragment<AuthPage> {

    private ViewGroup root;
    private View progressContainer;
    private Button primaryButton;

    private TextInputLayout addressInputLayout;

    public EnterpriseLoginFragment() {
    }

    public static EnterpriseLoginFragment newInstance(AuthPage page) {
        EnterpriseLoginFragment f = new EnterpriseLoginFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PAGE, page);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (root != null)
            return root;

        root = (ViewGroup) inflater.inflate(R.layout.fragment_login_enterprise, container, false);
        primaryButton = root.findViewById(R.id.primaryButton);
        addressInputLayout = root.findViewById(R.id.addressInputLayout);
        TextView titleView = root.findViewById(R.id.title);
        progressContainer = root.findViewById(R.id.progressContainer);
        View helpLink = root.findViewById(R.id.helpLink);
        View connectButton = root.findViewById(R.id.primaryButton);

        helpLink.setOnClickListener(v -> mListener.showPage(new AuthPage(AuthPageType.Help, page)));

        connectButton.setOnClickListener(this::onConnectClick);

        progressContainer.setOnClickListener((v) -> {
        });

        new TextInputLayoutErrorWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                String url = addressInputLayout.getEditText().getText().toString();
                primaryButton.setEnabled(url.length() > 0);
                addressInputLayout.setErrorEnabled(false);
            }
        }
                .addTextInputLayout(addressInputLayout);

        if (savedInstanceState != null && savedInstanceState.containsKey(ENTERPRISE_SERVER_URI))
            addressInputLayout.getEditText().setText(savedInstanceState.getString(ENTERPRISE_SERVER_URI));
        else {
            String storedUri = PreferenceManager.getDefaultSharedPreferences(inflater.getContext()).getString(ENTERPRISE_SERVER_URI, null);
            if (storedUri != null)
                addressInputLayout.getEditText().setText(storedUri);
        }

        ImageView v = root.findViewById(R.id.logoSubtitle);
        if (page.subtitleDrawableId != 0)
            v.setImageResource(page.subtitleDrawableId);


        return root;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (addressInputLayout != null)
            outState.putString(ENTERPRISE_SERVER_URI, addressInputLayout.getEditText().getText().toString());
    }


    private void onConnectClick(View view) {
        String address = addressInputLayout.getEditText().getText().toString();
        if (!address.contains("://"))
            address = "http://" + address;

        if (!URLUtil.isValidUrl(address)) {
            addressInputLayout.setError("Please, type in valid server url");
            addressInputLayout.setErrorEnabled(true);
            return;
        }
        Uri uri = Uri.parse(address);

        progressContainer.setVisibility(View.VISIBLE);

        ProverEnterpriseTransport.getInstance().getServerStatus(uri, new INetworkRequestListener<ServerStatus>() {
            @Override
            public void onNetworkRequestDone(NetworkRequest request, ServerStatus responce) {
                progressContainer.setVisibility(View.GONE);
                PreferenceManager.getDefaultSharedPreferences(view.getContext()).edit()
                        .putString(ENTERPRISE_SERVER_URI, uri.toString())
                        .commit();
                showPage(new AuthPage(AuthPageType.ShowMainPage, page), root.getContext());
            }

            @Override
            public void onNetworkRequestCancel(NetworkRequest request) {
                progressContainer.setVisibility(View.GONE);
            }

            @Override
            public void onNetworkRequestError(NetworkRequest request, Exception e) {
                progressContainer.setVisibility(View.GONE);
                Snackbar.make(root, "Error connecting to server: " + e.getLocalizedMessage(), LENGTH_SHORT).show();
            }

            @Override
            public void onNetworkRequestStart(NetworkRequest request) {

            }
        });
    }
}
