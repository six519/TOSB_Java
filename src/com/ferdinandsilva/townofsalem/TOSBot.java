package com.ferdinandsilva.townofsalem;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class TOSBot implements KeyListener {
	private Display display;
	private Shell shell;
	private Browser browser;
	private Text commandText;
	private TOSClient client;
	
	public static final String APP_TITLE = "Town of Salem Bot";
	public static final int APP_WIDTH = 800;
	public static final int APP_HEIGHT = 500;

	public static void main(String[] args) {
		TOSBot tosBot = new TOSBot();
	}
	
	public TOSBot() {
		display = new Display();
		shell = new Shell(display);
		shell.setSize(APP_WIDTH, APP_HEIGHT);
		shell.setText(APP_TITLE);
		
		centerScreen();
		setupControls();
		
		shell.open();
		
		while (!shell.isDisposed()) {
			if(!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
		
	}
	
	private void centerScreen() {
		Monitor monitor = display.getPrimaryMonitor();
		Rectangle bounds = monitor.getBounds();
		Rectangle rectangle = shell.getBounds();
		
		int x = bounds.x + (bounds.width - rectangle.width) / 2;
		int y = bounds.y + (bounds.height - rectangle.height) / 2;
		
		shell.setLocation(x, y);
	}
	
	private void setupControls() {
		GridLayout gridLayout = new GridLayout(1, false);
		shell.setLayout(gridLayout);
		
		GridData gridData = new GridData(GridData.FILL_BOTH);
		browser = new Browser(shell, SWT.BORDER);
		browser.setLayoutData(gridData);
		
		//init browser
		browser.setText("<html><head><title>" + APP_TITLE + "</title></head><body><div id=\"main_content\" style=\"font-family: monospace; line-height: 20px;\"></div></body></html>");

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		commandText = new Text(shell, SWT.SINGLE | SWT.BORDER);
		commandText.setLayoutData(gridData);
		
		commandText.addKeyListener(this);
		commandText.setFocus();
		
		browser.addProgressListener(new ProgressListener() {
			
			@Override
			public void completed(ProgressEvent arg0) {
				//start client
				//client = new TOSClient(TOSBot.this);
				//client.start();
				setDisplayText("Type <span style=\"color: blue;\"><i>/connect <b>USERNAME PASSWORD</b></i></span> ...");
				setDisplayText("Type <span style=\"color: blue;\"><i>/help</i></span> for complete list of commands...");
			}
			
			@Override
			public void changed(ProgressEvent arg0) {
			}
		});
		
		client = new TOSClient(TOSBot.this);
	}
	
	public void setDisplayText(String text) {
		browser.execute("var node=document.createElement('div')");
		//browser.execute("var textNode=document.createTextNode('" + text + "')");
		//browser.execute("node.appendChild(textNode)");
		browser.execute("document.getElementById(\"main_content\").appendChild(node)");
		browser.execute("node.innerHTML = '" + text + "'");
		browser.execute("window.scrollTo(0, document.body.scrollHeight)");
	}
	
	@Override
	public void keyReleased(KeyEvent keyEvent) {
		if(keyEvent.keyCode == 13) {
			String[] commands = commandText.getText().split(" ");
			
			if(commands[0].equals("/connect")) {
				
				if(commands.length == 3) {
					client.start(commands[1], commands[2]);
				} else {
					setDisplayText("<b><span style=\"color: red;\">Invalid parameter/s...</span></b>");
				}
				
			} else if(commands[0].equals("/exit")) {
				if(client.isRunning()) {
					//make sure the client thread is stopped before exiting
					client.stop();
				}
				System.exit(0);
			} else {
				setDisplayText("<b><span style=\"color: red;\">Invalid command...</span></b>");
			}

			//clear text
			commandText.setText("");
		}
	}
	
	@Override
	public void keyPressed(KeyEvent keyEvent) {
		
	}
}
