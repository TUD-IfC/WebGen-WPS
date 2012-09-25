package ica.wps.server;

import java.io.File;

/**
 * Thread deletes unneccesary files from data directory.
 * @author	M. Wittensoeldner
 * @date	Created on 13.02.2007
 */
public class DataDeletionThread extends Thread {
	protected File					_dir;
	protected long					_lRemoveAgeHour;

	/**
	 * Constructor.
	 * @param dir						The directory where the files are located.
	 * @param lRemoveAgeHour			Files older than this amount of hours will be deleted.
	 */
	public DataDeletionThread(File dir,
								long lRemoveAgeHour) {
		_dir = dir;
		_lRemoveAgeHour = lRemoveAgeHour;
	}

	/**
	 * Runs the thread. The file will be deleted.
	 * @return void
	 */
    public void run() {
    	try {
			File[] arrFiles = _dir.listFiles();
			File outFile;
			int i = 0;
			while ((arrFiles != null) && (i < arrFiles.length)) {
				outFile = arrFiles[i++];
				if (System.currentTimeMillis()-outFile.lastModified() > _lRemoveAgeHour*60L*60L*1000L) {
					outFile.delete();
				}
			}
    	} catch (Exception ex) {    		
		} catch (Error er) {    		
		}
	}
}
