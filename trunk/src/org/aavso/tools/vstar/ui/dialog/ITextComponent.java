package org.aavso.tools.vstar.ui.dialog;

import javax.swing.JComponent;


public interface ITextComponent {

	/**
	 * @return the name
	 */
	public abstract String getName();

	/**
	 * @return the canBeEmpty
	 */
	public abstract boolean canBeEmpty();

	/**
	 * @return the readOnly
	 */
	public abstract boolean isReadOnly();

	/**
	 * @return the value
	 */
	public abstract String getValue();

	/**
	 * Set the component to be editable or not.
	 * 
	 * @param state
	 *            True or false for editability.
	 */
	public void setEditable(boolean state);
	
	/**
	 * Returns the UI component. 
	 */
	public JComponent getUIComponent();
}