package awqatty.b.calq;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction().replace(
				android.R.id.content, new SettingsFragment()
				).commit();
		
		if (getActionBar() != null)
			getActionBar().setDisplayHomeAsUpEnabled(true);
	}

}
