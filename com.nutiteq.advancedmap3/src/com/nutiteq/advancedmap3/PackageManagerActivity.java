package com.nutiteq.advancedmap3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nutiteq.advancedmap3.Const;
import com.nutiteq.advancedmap3.R;
import com.nutiteq.hellomap3.util.AssetCopy;
import com.nutiteq.packagemanager.NutiteqPackageManager;
import com.nutiteq.packagemanager.PackageInfo;
import com.nutiteq.packagemanager.PackageManagerListener;
import com.nutiteq.packagemanager.PackageStatus;
import com.nutiteq.packagemanager.PackageStatus.Action;
import com.nutiteq.ui.MapView;
import com.nutiteq.wrappedcommons.PackageInfoVector;
import com.nutiteq.wrappedcommons.StringVector;

/**
 * A sample demonstrating how to use offline package manager of the Nutiteq SDK.
 * 
 * On the first run a custom MBTiles package is imported (we call this 'base package'), this package
 * contains tiles up to zoom level 5 and is small enough to be embedded with the application.
 * 
 * Then the sample downloads the latest package list from Nutiteq online service,
 * displays this list and allows user to manage offline packages (download, update, delete them).
 * 
 * Note that the sample does not include actual MapView, but using download packages
 * is actually very similar to using other tile sources - SDK contains PackageManagerTileDataSource
 * that will automatically display all imported or downloaded packages. PackageManagerTileDataSource
 * needs PackageManager instance, so it is best to create a PackageManager instance at application level
 * share this instance between activities. 
 * 
 */
public class PackageManagerActivity extends ListActivity {

	/**
	 * A full package info, containing package info and status.
	 */
	private static class Package {
		final String packageName;
		final String packageId;
		final PackageInfo packageInfo;
		final PackageStatus packageStatus;
		
		Package(String packageName, PackageInfo packageInfo, PackageStatus packageStatus) {
			this.packageName = packageName;
			this.packageId = (packageInfo != null ? packageInfo.getPackageId() : null);
			this.packageInfo = packageInfo;
			this.packageStatus = packageStatus;
		}
	}
	
	/**
	 * A holder class for packages containing views for each row in list view.
	 */
	private static class PackageHolder {
		TextView nameView;
		TextView statusView;
		Button actionButton;
	}
	
	/**
	 * Adapter for displaying packages as a list.
	 */
	private class PackageAdapter extends ArrayAdapter<Package> {
		Context context;
		int layoutResourceId;
		ArrayList<Package> packages;
		
		public PackageAdapter(Context context, int layoutResourceId, ArrayList<Package> packages) {
			super(context, layoutResourceId, packages);
			this.context = context;
			this.layoutResourceId = layoutResourceId;
			this.packages = packages;
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View row = convertView;
			PackageHolder holder = null;

			// Create new holder object or reuse existing
			if (row == null) {
				LayoutInflater inflater = ((Activity) context).getLayoutInflater();
				row = inflater.inflate(layoutResourceId, parent, false);

				holder = new PackageHolder();
				holder.nameView = (TextView) row.findViewById(com.nutiteq.advancedmap3.R.id.package_name);
				holder.statusView = (TextView) row.findViewById(com.nutiteq.advancedmap3.R.id.package_status);
				holder.actionButton = (Button) row.findViewById(com.nutiteq.advancedmap3.R.id.package_action);

				row.setTag(holder);
			} else {
				holder = (PackageHolder) row.getTag();
			}

			// Report package name and size
			final Package pkg = packages.get(position);
			holder.nameView.setText(pkg.packageName);
			if (pkg.packageInfo != null) {
				String status = "available";
				if (pkg.packageInfo.getSize().longValue() < 1024 * 1024) {
					status += " (<1MB)";
				} else {
					status += " (" + pkg.packageInfo.getSize().longValue() / 1024 / 1024 + "MB)";				
				}

				// Check if the package is downloaded/is being downloaded (so that status is not null)
				if (pkg.packageStatus != null) {
					if (pkg.packageStatus.getAction() == Action.READY) {
						status = "ready";
						holder.actionButton.setText("RM");
						holder.actionButton.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								packageManager.startPackageRemove(pkg.packageInfo.getPackageId());
							}
						});
					} else if (pkg.packageStatus.getAction() == Action.WAITING) {
						status = "queued";
						holder.actionButton.setText("C");
						holder.actionButton.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								packageManager.cancelPackageTasks(pkg.packageInfo.getPackageId());
							}
						});
					} else {
						if (pkg.packageStatus.getAction() == Action.COPYING) {
							status = "copying";
						} else if (pkg.packageStatus.getAction() == Action.DOWNLOADING) {
							status = "downloading";
						} else if (pkg.packageStatus.getAction() == Action.REMOVING) {
							status = "removing";
						}
						status += " " + Integer.toString((int) pkg.packageStatus.getProgress()) + "%";
						if (pkg.packageStatus.isPaused()) {
							status = status + " (paused)";
							holder.actionButton.setText("R");
							holder.actionButton.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									packageManager.setPackagePriority(pkg.packageInfo.getPackageId(), 0);
								}
							});
						} else {
							holder.actionButton.setText("P");
							holder.actionButton.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									packageManager.setPackagePriority(pkg.packageInfo.getPackageId(), -1);
								}
							});
						}
					}
				} else {
					holder.actionButton.setText("DL");
					holder.actionButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							packageManager.startPackageDownload(pkg.packageInfo.getPackageId());
						}
					});
				}
				holder.statusView.setText(status);
			} else {
				holder.actionButton.setText(">");
				holder.actionButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						currentFolder = currentFolder + pkg.packageName + "/";
						updatePackages();
					}
				});
				holder.statusView.setText("");				
			}
			
			return row;
		}
	}
	
	/**
	 * Listener for package manager events.
	 */
	class PackageListener extends PackageManagerListener {
		@Override
		public void onPackageListUpdated() {
			updatePackages();
		}

		@Override
		public void onPackageListFailed() {
			updatePackages();
			displayToast("Failed to download package list");
		}

		@Override
		public void onPackageStatusChanged(String id, int version, PackageStatus status) {
			updatePackage(id);
		}

		@Override
		public void onPackageCancelled(String id, int version) {
			updatePackage(id);
		}

		@Override
		public void onPackageUpdated(String id, int version) {
			updatePackage(id);
		}

		@Override
		public void onPackageFailed(String id, int version) {
			updatePackage(id);
			displayToast("Failed to download package " + id + "/" + version);
		}
	}

	// We will import initial map package that contains tiles up to zoom level 5.
	// We need to define a unique id for this package that does not clash with predefined packages.
	private static final String BASE_PACKAGE_ID = "basepkg";

	private NutiteqPackageManager packageManager;
	private ArrayAdapter<Package> packageAdapter;
	private ArrayList<Package> packageArray = new ArrayList<Package>();
	
	private String currentFolder = ""; // Current 'folder' of the package, for example "Asia/"
	private String language = "en"; // Language for the package names. Most major languages are supported
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Register license
        MapView.RegisterLicense("XTUN3Q0ZBd2NtcmFxbUJtT1h4QnlIZ2F2ZXR0Mi9TY2JBaFJoZDNtTjUvSjJLay9aNUdSVjdnMnJwVXduQnc9PQoKcHJvZHVjdHM9c2RrLWlvcy0zLiosc2RrLWFuZHJvaWQtMy4qCnBhY2thZ2VOYW1lPWNvbS5udXRpdGVxLioKYnVuZGxlSWRlbnRpZmllcj1jb20ubnV0aXRlcS4qCndhdGVybWFyaz1ldmFsdWF0aW9uCnVzZXJLZXk9MTVjZDkxMzEwNzJkNmRmNjhiOGE1NGZlZGE1YjA0OTYK", getApplicationContext());
		
        // Create package manager
        File packageFolder = new File(getApplicationContext().getExternalFilesDir(null), "mappackages");
        if (!(packageFolder.mkdirs() || packageFolder.isDirectory())) {
        	Log.e(Const.LOG_TAG, "Could not create package folder!");
        }
        packageManager = new NutiteqPackageManager(getApplicationContext(), "nutiteq.mbstreets", packageFolder.getAbsolutePath());
        packageManager.setPackageManagerListener(new PackageListener());
    	packageManager.startPackageListDownload();

        // Initialize list view
        setContentView(R.layout.list);
        packageAdapter = new PackageAdapter(this, com.nutiteq.advancedmap3.R.layout.package_item_row, packageArray);
        getListView().setAdapter(packageAdapter);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		packageManager.start();

		// Check if initial package is imported
		boolean imported = false;
		PackageInfoVector packages = packageManager.getLocalPackages();
		for (int i = 0; i < packages.size(); i++) {
			if (packages.get(i).getPackageId().equals(BASE_PACKAGE_ID)) {
				imported = true;
				break;
			}
		}
		if (!imported) {
			Log.i(Const.LOG_TAG, "Importing initial package");
            File localDir = getExternalFilesDir(null);
            try {
				AssetCopy.copyAssetToSDCard(getAssets(), "basepkg.mbtiles", localDir.getAbsolutePath());
				packageManager.startPackageImport(BASE_PACKAGE_ID, 1, new File(localDir, "basepkg.mbtiles").getAbsolutePath());
			} catch (IOException e) {
				Toast.makeText(getApplication(), "Could not import initial map package", Toast.LENGTH_SHORT).show();
			}
			// TODO: basepkg.mbtiles can be removed once import is complete (listener)
		}
	}
	
	@Override
	public void onStop() {
		packageManager.stop(false);
		super.onStop();
	}
	
	@Override
	public void onBackPressed() {
		if (currentFolder.length() == 0) {
			super.onBackPressed();
		} else {
			currentFolder = currentFolder.substring(0, currentFolder.lastIndexOf('/', currentFolder.length() - 2) + 1);
			updatePackages();
		}
	}
	
	private ArrayList<Package> getPackages() {
		HashMap<String, Package> pkgs = new HashMap<String, Package>();
		PackageInfoVector packageInfoVector = packageManager.getServerPackages();
		for (int i = 0; i < packageInfoVector.size(); i++) {
			PackageInfo packageInfo = packageInfoVector.get(i);
			String packageId = packageInfo.getPackageId();
			if (packageId.equals(BASE_PACKAGE_ID)) {
				continue; // ignore base package
			}

			// Get the list of names for this package. Each package may have multiple names,
			// packages are grouped using '/' as a separator, so the the full name for Sweden
			// is "Europe/Northern Europe/Sweden". We display packages as a tree, so we need
			// to extract only relevant packages belonging to the current folder.
			StringVector packageNames = packageInfo.getNames(language);
			for (int j = 0; j < packageNames.size(); j++) {
				String packageName = packageNames.get(j);
				if (!packageName.startsWith(currentFolder)) {
					continue; // belongs to a different folder, so ignore
				}
				packageName = packageName.substring(currentFolder.length());
				int index = packageName.indexOf('/');
				Package pkg;
				if (index == -1) {
					// This is actual package
					PackageStatus packageStatus = packageManager.getLocalPackageStatus(packageInfo.getPackageId(), -1);
					pkg = new Package(packageName, packageInfo, packageStatus);
				} else {
					// This is package group
					packageName = packageName.substring(0, index);
					if (pkgs.containsKey(packageName)) {
						continue;
					}
					pkg = new Package(packageName, null, null);
				}
				pkgs.put(packageName, pkg);
			}
		}
		return new ArrayList<Package>(pkgs.values());
	}
	
	private void updatePackages() {
		if (packageAdapter == null) {
			return;
		}
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// Simply reload all packages from the scratch
				packageArray.clear();
				packageArray.addAll(getPackages());
				packageAdapter.notifyDataSetChanged();
			}
		});
	}
	
	private void updatePackage(final String packageId) {
		if (packageAdapter == null) {
			return;
		}
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// Try to find the view that refers to the given package. Update only this view.
				int start = getListView().getFirstVisiblePosition();
				int end = getListView().getLastVisiblePosition();
				for (int position = start; position <= end; position++) {
					Package pkg = null;
					try {
						pkg = (Package) getListView().getItemAtPosition(position);
					} catch(Exception e) {						
					}
					if (pkg != null) {
						if (packageId.equals(pkg.packageId)) {
							PackageStatus packageStatus = packageManager.getLocalPackageStatus(packageId, -1);
							pkg = new Package(pkg.packageName, pkg.packageInfo, packageStatus);
							packageArray.set(position, pkg);
							packageAdapter.getView(position, getListView().getChildAt(position - start), getListView());
							break;
						}
					}
				}
			}
		});
	}
	
	private void displayToast(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show();
			}
		});
	}
}
