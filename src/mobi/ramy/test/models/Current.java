package mobi.ramy.test.models;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.content.SharedPreferences;

public class Current {

	public Venue[] _getVenues() 
	{
		return (ins==null||ins.venues==null) ?null :ins.venues;
	}
	
	public Venue _getVenue(String id) {
		if(ins==null||ins.venues==null) return null;
		for(int i=0;i<ins.venues.length;i++) {
			if(ins.venues[i].getID().equals(id)) {
				return ins.venues[i];
			}
		}
		return null;
	}
	public Venue[] _setVenues(Venue[] venues) {
		ins.venues = venues;
		save(); //never forget to save after setting persistent data
		return (ins==null||ins.venues==null) ? null: ins.venues;
	}
	public void _setUser(String id, String firstname, String lastname) {
		ins.facebookid = id;
		ins.firstname = firstname;
		ins.lastname = lastname;
		save(); //never forget to save after setting persistent data
	}
	public String _getFacebookId() {
		return ins.facebookid;
	}
	public String _getFirstName() {
		return ins.firstname;
	}
	public String _getLastName() {
		return ins.lastname;
	}
	private boolean saveDefaults(SharedPreferences prefs) {

		return true;
	}
	private static Current singleton;
	private Current(Context cxt) {
		mAppCxt = cxt.getApplicationContext();
		ins = load(mAppCxt);
	}
	public static Current get(Context cxt) {
		if (singleton == null) {
			singleton = new Current(cxt);
		}
		return singleton;
	}

	private Instance ins;
	private Context mAppCxt;
	private static final String CACHE_PREFS = "data.dat";
	private static final String CACHE_FILE = "data.dat";
	private static ExecutorService mExecutor;

	private Instance load(Context cxt) {
		Instance instance = null;
		// lets try to load it
		try {
			FileInputStream fis;
			ObjectInputStream ois;
			fis = cxt.openFileInput(CACHE_FILE);
			ois = new ObjectInputStream(fis);
			Object object = ois.readObject();
			if (Instance.class.isAssignableFrom(object.getClass())) {
				instance = (Instance) object;
			} else {
				throw new Exception("oof cant read!");
			}
			ois.close();
			fis.close();
		} catch (Exception ex) {
			return new Instance();
		}
		return instance;
	}
	private void save() {
		if (mExecutor == null) {
			mExecutor = Executors.newSingleThreadExecutor();
		}
		mExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					saveDefaults(mAppCxt.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE));
					FileOutputStream fos;
					ObjectOutputStream oos;
					fos = mAppCxt.openFileOutput(CACHE_FILE, Context.MODE_PRIVATE);
					oos = new ObjectOutputStream(fos);
					oos.writeObject(ins);
					oos.flush();
					fos.flush();
					oos.close();
					fos.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}
	private static class Instance implements Serializable {

		private static final long serialVersionUID = 5799876465878693539L;
		public Venue[] venues = null;
		private String facebookid = null;
		private String firstname = null;
		private String lastname = null;
	}
}