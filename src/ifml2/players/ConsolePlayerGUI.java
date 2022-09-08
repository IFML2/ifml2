package ifml2.players;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;

public class ConsolePlayerGUI
{
	/**
	 * @param args  Not used yet
	 */
	public static void main(String[] args)
	{
		JFileChooser ifmlFileChooser = new JFileChooser();
		ifmlFileChooser.setFileFilter(new FileFilter()
		{
			@Override
			public String getDescription()
			{
				return "Файл истории";
			}

			@Override
			public boolean accept(File f)
			{
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".xml");
			}
		});

        if(ifmlFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
		{
			ConsolePlayer.main(new String[] {ifmlFileChooser.getSelectedFile().getAbsolutePath()});
		}
	}

}
