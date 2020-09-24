package org.wordpress.android.ui.posts;

import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import org.wordpress.android.R;
import org.wordpress.android.WordPress;
import org.wordpress.android.fluxc.model.SiteModel;

import org.wordpress.android.ui.prefs.SiteSettingsInterface;
import org.wordpress.android.ui.prefs.WPSwitchPreference;
import org.wordpress.android.util.WPPrefUtils;

@SuppressWarnings("deprecation")
public class JetpackSecuritySettingsFragment extends PreferenceFragment
        implements SiteSettingsInterface.SiteSettingsListener,
        Preference.OnPreferenceChangeListener{

    private static final long FETCH_DELAY = 1000;

    public SiteModel mSite;

    // Can interface with WP.com or WP.org
    public SiteSettingsInterface mSiteSettings;

    // Used to ensure that settings are only fetched once throughout the lifecycle of the fragment
    private boolean mShouldFetch;

    // Jetpack settings
    private WPSwitchPreference mJpMonitorActivePref;
    private WPSwitchPreference mJpMonitorEmailNotesPref;
    private WPSwitchPreference mJpMonitorWpNotesPref;
    private WPSwitchPreference mJpBruteForcePref;
    private WPSwitchPreference mJpSsoPref;
    private WPSwitchPreference mJpMatchEmailPref;
    private WPSwitchPreference mJpUseTwoFactorPref;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.jetpack_settings);

        mSite = (SiteModel) getActivity().getIntent().getSerializableExtra(WordPress.SITE);

        if (mSite != null) {

            // setup state to fetch remote settings
            mShouldFetch = true;

            // initialize the appropriate settings interface (WP.com or WP.org)
            mSiteSettings = SiteSettingsInterface.getInterface(getActivity(), mSite, this);
        }

        // toggle which preferences are shown and set references
        initPreferences();
    }

    @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                       @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        //mDispatcher.register(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // always load cached settings
        mSiteSettings.init(false);

        if (mShouldFetch) {
            new Handler().postDelayed(() -> {
                // initialize settings with locally cached values, fetch remote on first pass
                mSiteSettings.init(true);
            }, FETCH_DELAY);
            // stop future calls from fetching remote settings
            mShouldFetch = false;
        }
    }

    @Override
    public void onDestroy() {
        if (mSiteSettings != null) {
            mSiteSettings.clear();
        }
        super.onDestroy();
    }

    // SiteSettingsListener
    @Override
    public void onSaveError(Exception error) {

    }

    @Override
    public void onFetchError(Exception error) {

    }

    @Override
    public void onSettingsUpdated() {
        if (isAdded()) {
            setPreferencesFromSiteSettings();
        }
    }

    @Override
    public void onSettingsSaved() {

    }

    @Override
    public void onCredentialsValidated(Exception error) {

    }

    /**
     * Helper method to retrieve {@link Preference} references and initialize any data.
     */
    public void initPreferences() {
        mJpMonitorActivePref = (WPSwitchPreference) getChangePref(R.string.pref_key_jetpack_monitor_uptime);
        mJpMonitorEmailNotesPref =
                (WPSwitchPreference) getChangePref(R.string.pref_key_jetpack_send_email_notifications);
        mJpMonitorWpNotesPref = (WPSwitchPreference) getChangePref(R.string.pref_key_jetpack_send_wp_notifications);
        mJpSsoPref = (WPSwitchPreference) getChangePref(R.string.pref_key_jetpack_allow_wpcom_sign_in);
        mJpBruteForcePref = (WPSwitchPreference) getChangePref(R.string.pref_key_jetpack_prevent_brute_force);
        mJpMatchEmailPref = (WPSwitchPreference) getChangePref(R.string.pref_key_jetpack_match_via_email);
        mJpUseTwoFactorPref = (WPSwitchPreference) getChangePref(R.string.pref_key_jetpack_require_two_factor);
    }

    public void setPreferencesFromSiteSettings() {
        mJpMonitorActivePref.setChecked(mSiteSettings.isJetpackMonitorEnabled());
        mJpMonitorEmailNotesPref.setChecked(mSiteSettings.shouldSendJetpackMonitorEmailNotifications());
        mJpMonitorWpNotesPref.setChecked(mSiteSettings.shouldSendJetpackMonitorWpNotifications());
        mJpBruteForcePref.setChecked(mSiteSettings.isJetpackProtectEnabled());
        mJpSsoPref.setChecked(mSiteSettings.isJetpackSsoEnabled());
        mJpMatchEmailPref.setChecked(mSiteSettings.isJetpackSsoMatchEmailEnabled());
        mJpUseTwoFactorPref.setChecked(mSiteSettings.isJetpackSsoTwoFactorEnabled());
    }

    private Preference getChangePref(int id) {
        return WPPrefUtils.getPrefAndSetChangeListener(this, id, this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (newValue == null) {
            return false;
        }

        if (preference == mJpMonitorActivePref) {
            mJpMonitorActivePref.setChecked((Boolean) newValue);
            mSiteSettings.enableJetpackMonitor((Boolean) newValue);
        } else if (preference == mJpMonitorEmailNotesPref) {
            mJpMonitorEmailNotesPref.setChecked((Boolean) newValue);
            mSiteSettings.enableJetpackMonitorEmailNotifications((Boolean) newValue);
        } else if (preference == mJpMonitorWpNotesPref) {
            mJpMonitorWpNotesPref.setChecked((Boolean) newValue);
            mSiteSettings.enableJetpackMonitorWpNotifications((Boolean) newValue);
        } else if (preference == mJpBruteForcePref) {
            mJpBruteForcePref.setChecked((Boolean) newValue);
            mSiteSettings.enableJetpackProtect((Boolean) newValue);
        } else if (preference == mJpSsoPref) {
            mJpSsoPref.setChecked((Boolean) newValue);
            mSiteSettings.enableJetpackSso((Boolean) newValue);
        } else if (preference == mJpMatchEmailPref) {
            mJpMatchEmailPref.setChecked((Boolean) newValue);
            mSiteSettings.enableJetpackSsoMatchEmail((Boolean) newValue);
        } else if (preference == mJpUseTwoFactorPref) {
            mJpUseTwoFactorPref.setChecked((Boolean) newValue);
            mSiteSettings.enableJetpackSsoTwoFactor((Boolean) newValue);
        }

        mSiteSettings.saveSettings();

        return true;
    }
}
