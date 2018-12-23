import java.awt.ComponentOrientation;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.json.JSONException;

import com.sun.tools.javac.Main;

public class YouTube2mp3 extends JPanel {
	private static final long serialVersionUID = 1L;
	private JTextField url;
	private JButton convert, download;
	private JLabel image, title;
	private String id = "", fileName;
	double currentPrecentage = 0;

	public YouTube2mp3() {
		super();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		url = new JTextField("הכנס כתובת");
		url.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		convert = new JButton("המר");
		download = new JButton("הורד");
		download.setEnabled(false);
		image = new JLabel();
		title = new JLabel();
		title.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		url.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e))
					url.setText("");
				else if (SwingUtilities.isRightMouseButton(e)) {
					try {
						url.setText((String) Toolkit.getDefaultToolkit().getSystemClipboard()
								.getData(DataFlavor.stringFlavor));
						convert.doClick();
						convert.setEnabled(true);
						download.setEnabled(false);
					} catch (HeadlessException | UnsupportedFlavorException | IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		url.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ke) {
				convert.setEnabled(true);
				convert.doClick();
				download.setEnabled(false);
			}
		});

		convert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (isNotNullNotEmptyNotWhiteSpaceOnlyByJava(url.getText())
						&& isNotNullNotEmptyNotWhiteSpaceOnlyByJava(getId())) {
					url.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
					try {
						image.setIcon(
								new ImageIcon(new URL("http://img.youtube.com/vi/" + getId() + "/mqdefault.jpg")));
					} catch (MalformedURLException e2) {
						e2.printStackTrace();
					}
					try {
						fileName = JsonReader
								.readJsonFromUrl("https://noembed.com/embed?url=https://www.youtube.com/watch?v=" + id)
								.getString("title");
					} catch (JSONException | IOException e1) {
						JOptionPane.showMessageDialog(getRootPane().getContentPane(), "כתובת לא תקינה",
								"שגיאה: כתובת לא תקינה", JOptionPane.ERROR_MESSAGE);
						title.setText("");
					}
					title.setText(fileName);

				} else {
					JOptionPane.showMessageDialog(getRootPane().getContentPane(), "כתובת לא תקינה",
							"שגיאה: כתובת לא תקינה", JOptionPane.ERROR_MESSAGE);
					title.setText("");
				}

				if (image.getIcon() != null) {
					getRootPane().getParent().setSize(620, 300);
					download.setEnabled(true);
					convert.setEnabled(false);
				}
			}
		});

		download.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						try {
							if (isNotNullNotEmptyNotWhiteSpaceOnlyByJava(url.getText())
									&& isNotNullNotEmptyNotWhiteSpaceOnlyByJava(id)) {
								JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home")) {
									@Override
									public void approveSelection() {
										File fileToSave = getSelectedFile();
										if (fileToSave.exists() && getDialogType() == SAVE_DIALOG) {
											int result = JOptionPane.showConfirmDialog(this,
													"The file exists, overwrite?", "Existing file",
													JOptionPane.YES_NO_CANCEL_OPTION);
											switch (result) {
											case JOptionPane.YES_OPTION:
												super.approveSelection();
												return;
											case JOptionPane.NO_OPTION:
												return;
											case JOptionPane.CLOSED_OPTION:
												return;
											case JOptionPane.CANCEL_OPTION:
												cancelSelection();
												return;
											}
										}
										super.approveSelection();
									}
								};
								fileChooser.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
								fileChooser.setDialogTitle("שמור בשם...");
								fileChooser.setSelectedFile(new File(fileName.replace("|", "") + ".mp3"));
								fileChooser.setFileFilter(new FileNameExtensionFilter("קבצי mp3", "mp3"));
								int userSelection = fileChooser.showSaveDialog(null);
								if (userSelection == JFileChooser.APPROVE_OPTION) {
									File fileToSave = fileChooser.getSelectedFile();
									downloadFile(fileToSave.getAbsolutePath());
									JOptionPane.showMessageDialog(getRootPane().getParent(),
											"ההורדה הושלמה! \n" + "הקובץ נשמר במיקום:\n" + fileToSave.getAbsolutePath(),
											"ההורדה הושלמה!", JOptionPane.INFORMATION_MESSAGE);
								}
							}
						} catch (IOException | InterruptedException e) {
							e.printStackTrace();
						}
					}
				};
				thread.start();
			}
		});

		url.setBounds(10, 10, 430, 30);
		convert.setBounds(465, 10, 60, 30);
		download.setBounds(530, 10, 60, 30);
		title.setBounds(10, 230, 430, 39);
		image.setBounds(120, 51, 320, 180);

		this.setLayout(null);
		this.add(url);
		this.add(convert);
		this.add(download);
		this.add(image);
		this.add(title);
	}

	protected void downloadFile(String name) throws IOException, InterruptedException {
		if (!name.toLowerCase().endsWith(".mp3"))
			name += ".mp3";
		URL downloadURL = new URL("https://www.convertmp3.io/fetch/?video=https://www.youtube.com/watch?v=" + getId());
		
		System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
		final HttpsURLConnection httpcon = (HttpsURLConnection) downloadURL.openConnection();
		httpcon.addRequestProperty("User-Agent", "Mozilla/4.76");
		final double size = httpcon.getContentLength();
		InputStream in = httpcon.getInputStream();
		final String encoding = httpcon.getContentEncoding();
		if (encoding != null) {
			if ("gzip".equalsIgnoreCase(encoding)) {
				System.out.println("**");
				in = new GZIPInputStream(in);
			} else {
				System.err.println("WARN: unsupported Content-Encoding: " + encoding);
			}
		}
		JProgressBar bar = new JProgressBar(0, 100);
		bar.setVisible(true);
		bar.setStringPainted(true);
		JFrame frameBar = new JFrame("הקובץ בהורדה");
		frameBar.setLocation(getRootPane().getParent().getLocation());
		frameBar.add(new JPanel().add(bar));
		frameBar.pack();
		frameBar.setSize(500, 100);

		frameBar.setVisible(true);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024 * 8];
		int r;
		while ((r = in.read(buf)) != -1) {
			bos.write(buf, 0, r);
			currentPrecentage = (double) (bos.size() / size) * 100;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					bar.setValue((int) currentPrecentage);
					bar.setString((int)currentPrecentage + "%");
					
				}
			});
		}
		in.close();

		final byte[] response = bos.toByteArray();

		FileOutputStream fos = new FileOutputStream(name);
		fos.write(response);
		frameBar.dispose();
		fos.close();
	}

	protected String getId() {
		String pattern = "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
		Pattern compiledPattern = Pattern.compile(pattern);
		Matcher matcher = compiledPattern.matcher(url.getText());
		if (matcher.find()) {
			return id = matcher.group();
		}
		return id;
	}

	public static boolean isNotNullNotEmptyNotWhiteSpaceOnlyByJava(final String string) {
		return string != null && !string.isEmpty() && !string.trim().isEmpty();
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("הורדת שירים מיוטיוב");
		try {
			frame.setIconImage(ImageIO.read(new File("res/ico.png")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JFrame.setDefaultLookAndFeelDecorated(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
		int scrennHieght = Toolkit.getDefaultToolkit().getScreenSize().height;
		frame.setBounds(screenWidth / 2 - 310, scrennHieght / 2 - 150, 620, 100);
		frame.setResizable(true);
		frame.getContentPane().add(new YouTube2mp3());
		frame.setVisible(true);
	}

}