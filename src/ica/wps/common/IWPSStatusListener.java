package ica.wps.common;

/**
 * Interface defines a WPS status listener.
 * @author	M. Wittensoeldner
 * @date	Created on 10.02.2007
 */
public interface IWPSStatusListener {

	/**
	 * Sets the progress status.
	 * @return void
	 * @param dStatus							The progress status. Its a value between 0.0 and 1.0.
	 * @param sStatusText						The progress status text. Can be null.
	 */
	public void setStatus(double dStatus, String sStatusText);
}

