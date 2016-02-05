package com.nutiteq.advancedmap3;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nutiteq.datasources.PackageManagerTileDataSource;
import com.nutiteq.packagemanager.NutiteqPackageManager;
import com.nutiteq.packagemanager.PackageErrorType;
import com.nutiteq.packagemanager.PackageInfo;
import com.nutiteq.packagemanager.PackageManagerListener;
import com.nutiteq.packagemanager.PackageStatus;
import com.nutiteq.packagemanager.PackageAction;
import com.nutiteq.ui.MapView;
import com.nutiteq.wrappedcommons.PackageInfoVector;
import com.nutiteq.wrappedcommons.StringVector;

/**
 * A sample demonstrating how to use offline package manager of the Nutiteq SDK.
 * 
 * The sample downloads the latest package list from Nutiteq online service,
 * displays this list and allows user to manage offline packages (download, update, delete them).
 * 
 * Note that the sample does not include MapView, but using download packages
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
					if (pkg.packageStatus.getCurrentAction() == PackageAction.PACKAGE_ACTION_READY) {
						status = "ready";
						holder.actionButton.setText("RM");
						holder.actionButton.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								packageManager.startPackageRemove(pkg.packageInfo.getPackageId());
							}
						});
					} else if (pkg.packageStatus.getCurrentAction() == PackageAction.PACKAGE_ACTION_WAITING) {
						status = "queued";
						holder.actionButton.setText("C");
						holder.actionButton.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								packageManager.cancelPackageTasks(pkg.packageInfo.getPackageId());
							}
						});
					} else {
						if (pkg.packageStatus.getCurrentAction() == PackageAction.PACKAGE_ACTION_COPYING) {
							status = "copying";
						} else if (pkg.packageStatus.getCurrentAction() == PackageAction.PACKAGE_ACTION_DOWNLOADING) {
							status = "downloading";
						} else if (pkg.packageStatus.getCurrentAction() == PackageAction.PACKAGE_ACTION_REMOVING) {
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
		public void onPackageFailed(String id, int version, PackageErrorType errorType) {
			updatePackage(id);
			displayToast("Failed to download package " + id + "/" + version + ": " + errorType);
		}
	}

	private NutiteqPackageManager packageManager;
	private ArrayAdapter<Package> packageAdapter;
	private ArrayList<Package> packageArray = new ArrayList<Package>();
	
	private String currentFolder = ""; // Current 'folder' of the package, for example "Asia/"
	private String language = "en"; // Language for the package names. Most major languages are supported
    public static PackageManagerTileDataSource dataSource;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Register license
        MapView.registerLicense("XTUN3Q0ZBd2NtcmFxbUJtT1h4QnlIZ2F2ZXR0Mi9TY2JBaFJoZDNtTjUvSjJLay9aNUdSVjdnMnJwVXduQnc9PQoKcHJvZHVjdHM9c2RrLWlvcy0zLiosc2RrLWFuZHJvaWQtMy4qCnBhY2thZ2VOYW1lPWNvbS5udXRpdGVxLioKYnVuZGxlSWRlbnRpZmllcj1jb20ubnV0aXRlcS4qCndhdGVybWFyaz1ldmFsdWF0aW9uCnVzZXJLZXk9MTVjZDkxMzEwNzJkNmRmNjhiOGE1NGZlZGE1YjA0OTYK", getApplicationContext());
		
        // Create package manager
        File packageFolder = new File(getApplicationContext().getExternalFilesDir(null), "mappackages");
        if (!(packageFolder.mkdirs() || packageFolder.isDirectory())) {
        	Log.e(Const.LOG_TAG, "Could not create package folder!");
        }
        packageManager = new NutiteqPackageManager("nutiteq.osm", packageFolder.getAbsolutePath());
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
	}
	
	@Override
	public void onStop() {
		packageManager.stop(false);
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		packageManager.setPackageManagerListener(null);
		super.onDestroy();
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
				// Try to find the package that needs to be updated
				for (int i = 0; i < packageArray.size(); i++) {
					Package pkg = packageArray.get(i);
					if (packageId.equals(pkg.packageId)) {
						PackageStatus packageStatus = packageManager.getLocalPackageStatus(packageId, -1);
						pkg = new Package(pkg.packageName, pkg.packageInfo, packageStatus);
						packageArray.set(i, pkg);
						// TODO: it would be much better to only refresh the changed row
						packageAdapter.notifyDataSetChanged();
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
	
	@SuppressLint({ "InlinedApi", "NewApi" })
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    
	    MenuItem menuItem = menu.add("Map").setOnMenuItemClickListener(new OnMenuItemClickListener(){
            @Override
            public boolean onMenuItemClick (MenuItem item){
               
                // Using static global variable to pass data. Avoid this in your app (memory leaks etc)!
                PackageManagerActivity.dataSource = new PackageManagerTileDataSource(PackageManagerActivity.this.packageManager);
                
                Intent myIntent = new Intent(PackageManagerActivity.this,
                        PackagedMapActivity.class);
                PackageManagerActivity.this.startActivity(myIntent);
                
                return true;
            }
        });
	    
	    menuItem.setIcon(android.R.drawable.ic_dialog_map);
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	    }
	    return super.onCreateOptionsMenu(menu);
	}
}
