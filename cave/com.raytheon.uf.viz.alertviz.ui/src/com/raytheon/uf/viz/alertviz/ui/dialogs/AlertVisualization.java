/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 *
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 *
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 *
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.viz.alertviz.ui.dialogs;

import java.io.File;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.ui.themes.ColorUtil;

import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationUtil;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.message.StatusMessage;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.alertviz.AlertvizJob;
import com.raytheon.uf.viz.alertviz.ConfigContext;
import com.raytheon.uf.viz.alertviz.ConfigurationManager;
import com.raytheon.uf.viz.alertviz.Constants;
import com.raytheon.uf.viz.alertviz.Container;
import com.raytheon.uf.viz.alertviz.IAlertArrivedCallback;
import com.raytheon.uf.viz.alertviz.IConfigurationChangedListener;
import com.raytheon.uf.viz.alertviz.IRestartListener;
import com.raytheon.uf.viz.alertviz.config.AlertMetadata;
import com.raytheon.uf.viz.alertviz.config.Category;
import com.raytheon.uf.viz.alertviz.config.Configuration;
import com.raytheon.uf.viz.alertviz.config.Source;
import com.raytheon.uf.viz.alertviz.config.TrayConfiguration;
import com.raytheon.uf.viz.alertviz.python.AlertVizPython;
import com.raytheon.uf.viz.alertviz.ui.audio.AlertAudioMgr;
import com.raytheon.uf.viz.alertviz.ui.audio.IAudioAction;
import com.raytheon.uf.viz.alertviz.ui.timer.AlertTimer;
import com.raytheon.uf.viz.alertviz.ui.timer.ITimerAction;
import com.raytheon.uf.viz.core.VizApp;

/**
 * This is the main class for the alert visualization.
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- --------------------------------------------
 * Oct 05, 2008           lvenable  Initial creation.
 * Apr 02, 2009           lvenable  TTR fixes.
 * Nov 18, 2010  2235     cjeanbap  Add AlertViz location preservation.
 * Dec 01, 2010  6532     cjeanbap  Add functionality to restart
 *                                  AlertVisualization App.
 * Jan 21, 2011           cjeanbap  Check Status Message for Internal AlertViz
 *                                  problem(s).
 * Jan 24, 2011  1978     cjeanbap  Add Monitor Tooltip functionality.
 * Jan 28, 2011  4617     cjeanbap  Added Monitor Only functionality.
 * Feb 08, 2011  4617     cjeanbap  Fix bug of multiple windows.
 * Feb 14, 2010  4617     cjeanbap  Changed handling of Exceptions filtering.
 * Mar 03, 2011  8059     rferrel   alertArrived can now play system beep.
 * Mar 04, 2011  6532     rferrel   Restart now works
 * May 02, 2011  9067     cjeanbap  Redraw AlertMessageDlg if a Layout or a
 *                                  Monitor was changed.
 * May 03, 2011  9101     cjeanbap  Pass a clone object into AlertVizPython
 *                                  class.
 * May 31, 2011  8058     cjeanbap  Kill sound based on TextMsgBox id.
 * Jan 17, 2012  27       rferrel   Refactored to allow override of
 *                                  createTrayMenuItems
 * Mar 09, 2015  3856     lvenable  Added a check to determine if the timer is
 *                                  running before changing the icon on the
 *                                  timer action.  If it isn't running then set
 *                                  the icon to the default image.
 * Mar 18, 2015  4234     njensen   Remove reference to non-working python
 * Jun 03, 2015  4473     njensen   Updated for new AlertvizJob API
 * Jun 29, 2015  4311     randerso  Reworking AlertViz dialogs to be resizable.
 * Oct 28, 2015  5054     randerso  Call AlertVisualization.dispose() on restart
 *                                  so all the other dispose methods are called.
 * Jan 25, 2016  5054     randerso  Removed dummy parent shell
 * Feb 08, 2016  5312     randerso  Changed to build tray menu on demand
 * Feb 14, 2017  6029     randerso  Make popup appear on monitor with AlertViz
 *                                  bar
 * Mar 24, 2017  DR 16985 dfiedman  Restore Python script functionality
 *
 * </pre>
 *
 * @author lvenable
 *
 */
public class AlertVisualization
        implements ITimerAction, IAudioAction, IAlertArrivedCallback, Listener,
        IConfigurationChangedListener, IRestartListener {
    /**
     * The display control.
     */
    private final Display display;

    /**
     * Alert visualization image.
     */
    private Image alertVizImg;

    /**
     * Alert visualization error image.
     */
    private Image alertVizErrorImg;

    /**
     * System tray object.
     */
    private Tray tray;

    /**
     * Tray item.
     */
    private TrayItem trayItem;

    /**
     * Alert message dialog.
     */
    protected AlertMessageDlg alertMessageDlg;

    /**
     * Text blink count variable.
     */
    private int blinkCount = 0;

    /**
     * Array of blink images.
     */
    private Image[] blinkImages;

    /**
     * Timer for the tray to blink the icon.
     */
    private AlertTimer trayAlertTimer;

    /**
     * Audio manager used to set the audio file and play sounds.
     */
    private AlertAudioMgr audioMgr;

    /**
     * Configuration data.
     */
    private Configuration configData;

    /**
     * Alert popup dialog.
     */
    private AlertPopupMessageDlg alertPopupDlg;

    /**
     * Configuration dialog.
     */
    private AlertVisConfigDlg configDlg;

    /**
     * Do not disturb flag.
     */
    private boolean doNotDisturb = false;

    /**
     * Show alert Dialog flag
     */
    private boolean showAlertDlg = true;

    private boolean ackAll = false;

    private boolean showPopup = false;

    /**
     * Tool tip.
     */
    private ToolTip toolTip;

    /**
     * Is this running as a standalone application
     */
    protected final boolean runningStandalone;

    private ConfigContext configContext;

    private Configuration prevConfigFile;

    private Integer exitStatus = IApplication.EXIT_OK;

    /**
     * Constructor.
     *
     * @param runningStandalone
     *            True if the application is running stand-alone.
     * @param display
     *            Display object.
     */
    public AlertVisualization(boolean runningStandalone, Display display) {
        this.display = display;
        this.runningStandalone = runningStandalone;
        ConfigurationManager.getInstance().addListener(this);
        if (Boolean.getBoolean("SystemTray")) {
            showAlertDlg = Boolean.getBoolean("ShowAlertVizBar");
            doNotDisturb = true;
        }
        initializeComponents();

        AlertvizJob.getInstance().addAlertArrivedCallback(this);
    }

    /**
     * Dispose method.
     */
    public void dispose() {

        if (alertPopupDlg != null) {
            alertPopupDlg.dispose();
        }

        if (trayAlertTimer != null) {
            cancelTimer();
        }

        if (audioMgr != null) {
            int numTextBoxComp = getNumberOfTextControls();
            for (int i = 0; i < numTextBoxComp; i++) {
                audioMgr.stopTimer(i);
            }
        }

        if (toolTip != null) {
            toolTip.dispose();
        }

        if (alertVizImg != null) {
            alertVizImg.dispose();
        }

        if (alertVizErrorImg != null) {
            alertVizErrorImg.dispose();
        }

        if (alertMessageDlg != null) {
            alertMessageDlg.dispose();
        }

        if (display != null) {
            display.dispose();
        }

    }

    /**
     * Initialize all of the components and data.
     */
    private void initializeComponents() {
        configurationChanged();
        audioMgr = new AlertAudioMgr(display, getNumberOfTextControls());
        trayAlertTimer = new AlertTimer(display, this, 1000);

        initAlertMessageDialog();
        initTrayControl();
        prevConfigFile = configData.clone();
    }

    /**
     * Initialize the alert message dialog.
     */
    private void initAlertMessageDialog() {
        alertMessageDlg = new AlertMessageDlg(display, this, showAlertDlg,
                configData, audioMgr);
        display.syncExec(new Runnable() {
            @Override
            public void run() {
                alertMessageDlg.open();
            }
        });
    }

    /**
     * Initialize the tray control.
     */
    private void initTrayControl() {
        blinkImages = new Image[2];

        alertVizImg = new Image(display, loadAlertVizImage());
        alertVizErrorImg = new Image(display, loadAlertVizErrorImage());

        blinkImages[0] = alertVizImg;
        blinkImages[1] = alertVizErrorImg;

        tray = display.getSystemTray();

        if (tray == null) {
            Container.logInternal(Priority.ERROR,
                    "The system tray is not available");
        } else {
            createTray();
        }
    }

    /**
     * Create the tray control.
     */
    private void createTray() {
        trayItem = new TrayItem(tray, SWT.NONE);
        updateToolTip();

        // Right click action
        trayItem.addMenuDetectListener(new MenuDetectListener() {
            @Override
            public void menuDetected(MenuDetectEvent de) {
                Menu trayItemMenu = new Menu(alertMessageDlg.getShell(),
                        SWT.POP_UP);
                createTrayMenuItems(trayItemMenu);
                trayItemMenu.setVisible(true);
            }
        });

        // Left click action
        trayItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (alertPopupDlg != null) {
                    openAlertPopupDialog();
                }
            }
        });

        trayItem.setImage(alertVizImg);
    }

    /**
     * Create the tray menu items.
     *
     * @param menu
     */
    protected void createTrayMenuItems(Menu menu) {

        MenuItem showAlertDialogMI = new MenuItem(menu, SWT.CHECK);
        showAlertDialogMI.setText("Show Alert Dialog");
        showAlertDialogMI.setSelection(showAlertDlg);
        showAlertDialogMI.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                if (alertMessageDlg != null) {
                    showAlertDlg = !showAlertDlg;
                    alertMessageDlg.showDialog(showAlertDlg);
                    if (Boolean.getBoolean("SystemTray")) {
                        System.setProperty("ShowAlertVizBar",
                                Boolean.toString(showAlertDlg));
                    }
                }
            }
        });

        if (Boolean.getBoolean("SystemTray")) {
            MenuItem doNotDisturbMI = new MenuItem(menu, SWT.CHECK);
            doNotDisturbMI.setText("Do Not Disturb");
            doNotDisturbMI.setSelection(doNotDisturb);
            doNotDisturbMI.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    MenuItem item = (MenuItem) event.widget;
                    doNotDisturb = item.getSelection();
                }
            });
        }

        new MenuItem(menu, SWT.SEPARATOR);

        MenuItem configTrayMI = new MenuItem(menu, SWT.NONE);
        configTrayMI.setText("Configuration...");
        configTrayMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                showConfigDialog();
            }
        });

        MenuItem viewLogMI = new MenuItem(menu, SWT.NONE);
        viewLogMI.setText("System Log...");
        viewLogMI.addSelectionListener(new SelectionAdapter() {
            SimpleLogViewer slv = null;

            @Override
            public void widgetSelected(SelectionEvent event) {
                if ((slv == null) || slv.isDisposed()) {
                    slv = new SimpleLogViewer(display);
                    slv.open();
                } else {
                    slv.bringToTop();
                }
            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        MenuItem showPopupMI = new MenuItem(menu, SWT.NONE);
        showPopupMI.setText("Show Alert Popup Dialog...");
        showPopupMI.setEnabled(showPopup && (alertPopupDlg != null)
                && !alertPopupDlg.isOpen());
        showPopupMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                openAlertPopupDialog();
            }
        });

        MenuItem ackAllMI = new MenuItem(menu, SWT.NONE);
        ackAllMI.setText("Acknowledge All Messages");
        ackAllMI.setEnabled(ackAll);
        ackAllMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (alertPopupDlg != null) {
                    alertPopupDlg.showDialog();
                    alertPopupDlg.acknowledgeAllMessages();
                } else {
                    // should never happen
                    Container.logInternal(Priority.ERROR,
                            "alertPopupDlg unexpectedly null");
                }
            }
        });

        if (this.runningStandalone) {
            new MenuItem(menu, SWT.SEPARATOR);
            MenuItem restartMI = new MenuItem(menu, SWT.NONE);
            restartMI.setText("Restart");
            restartMI.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    MessageDialog dialog = new MessageDialog(
                            alertMessageDlg.getShell(), "Confirm Restart!",
                            null,
                            "Any unsaved changes will be lost. Restart anyway?",
                            MessageDialog.QUESTION,
                            new String[] { IDialogConstants.YES_LABEL,
                                    IDialogConstants.NO_LABEL },
                            0);

                    dialog.create();

                    // center dialog on display
                    Shell shell = dialog.getShell();
                    Point size = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                    Rectangle bounds = alertMessageDlg.getShell().getMonitor()
                            .getBounds();
                    shell.setLocation(bounds.x + ((bounds.width - size.x) / 2),
                            bounds.y + ((bounds.height - size.y) / 2));
                    if (dialog.open() == 0) {
                        restart();
                    }
                }
            });
        }

    }

    /**
     * Start the tray icon blink timer.
     */
    private void startBlinkTrayTimer() {
        trayAlertTimer.startTimer(20000);
    }

    /**
     * Timer action to change the tray icons to emulate blinking.
     */
    @Override
    public void timerAction(boolean ignoreThisArg) {
        if (trayItem.isDisposed()) {
            return;
        }
        ++blinkCount;

        if (blinkCount >= blinkImages.length) {
            blinkCount = 0;
        }

        if (this.trayAlertTimer.timerIsRunning()) {
            trayItem.setImage(blinkImages[blinkCount]);
        } else {
            trayItem.setImage(alertVizImg);
        }
    }

    /**
     * Reset the tray icon when the blink timer is complete.
     */
    @Override
    public void timerCompleted() {
        trayItem.setImage(alertVizImg);
    }

    /**
     * Cancel playing the audio file.
     */
    @Override
    public void cancelAudio() {
        int numTextBoxComp = getNumberOfTextControls();
        for (int i = 0; i < numTextBoxComp; i++) {
            audioMgr.stopTimer(i);
        }
    }

    @Override
    public void cancelAudio(int numTextMsgBoxId) {
        audioMgr.stopTimer(numTextMsgBoxId);
    }

    /**
     * Load the alert visualization image.
     *
     * @return Image path.
     */
    private String loadAlertVizImage() {
        IPathManager pm = PathManagerFactory.getPathManager();
        String path = pm
                .getFile(
                        pm.getContext(LocalizationType.CAVE_STATIC,
                                LocalizationLevel.BASE),
                        "alertVizIcons" + File.separatorChar + "alertViz.png")
                .getAbsolutePath();
        return path;
    }

    /**
     * Load the alert visualization error image.
     *
     * @return Image path.
     */
    private String loadAlertVizErrorImage() {
        IPathManager pm = PathManagerFactory.getPathManager();
        String path = pm
                .getFile(
                        pm.getContext(LocalizationType.CAVE_STATIC,
                                LocalizationLevel.BASE),
                        "alertVizIcons" + File.separatorChar + "alertError.png")
                .getAbsolutePath();
        return path;
    }

    /**
     * Show the Alert Visualization Configuration dialog.
     */
    private void showConfigDialog() {
        if ((configDlg != null) && !configDlg.isDisposed()) {
            configDlg.close();
        }
        configDlg = new AlertVisConfigDlg(display, alertMessageDlg, configData,
                configContext, this, this);
        configDlg.open();
    }

    /**
     * Handle the alert message.
     *
     * @param statMsg
     *            Status message.
     * @param amd
     *            Alert metadata.
     * @param cat
     *            Category.
     * @param gConfig
     *            Global configuration.
     */
    @Override
    public void alertArrived(final StatusMessage statMsg,
            final AlertMetadata amd, final Category cat,
            final TrayConfiguration gConfig) {

        if ((alertMessageDlg == null) || alertMessageDlg.isDisposed()) {
            Container.logInternal(Priority.ERROR,
                    statMsg.getMessage() + "\n" + statMsg.getDetails());
            return;
        }

        if (amd.isPythonEnabled() && amd.getPythonScript() != null) {
            AlertVizPython.run(statMsg, amd.clone(), gConfig);
        }

        boolean isGdnAdminMessage = statMsg.getCategory().equals("GDN_ADMIN")
                || statMsg.getSourceKey().equals("GDN_ADMIN");

        if (isGdnAdminMessage) {
            // Container.logInternal(statMsg);
            if ((statMsg.getDetails() != null)
                    && (statMsg.getDetails().contains("Error")
                            || statMsg.getDetails().contains("Exception")
                            || statMsg.getDetails().contains("Throwable")
                            || Container.hasMissing(statMsg))) {
                Source source = configData.lookupSource("GDN_ADMIN");
                RGB backgroundRBG = null;
                if ((source == null)
                        || (source.getConfigurationItem() == null)) {
                    backgroundRBG = ColorUtil.getColorValue("COLOR_YELLOW");
                } else {
                    AlertMetadata am = source.getConfigurationItem()
                            .lookup(Priority.SIGNIFICANT);
                    backgroundRBG = am.getBackground();
                }
                alertMessageDlg.setErrorLogBtnBackground(backgroundRBG);
                alertMessageDlg.sendToTextMsgLog(statMsg);
            }
        } else {

            VizApp.runAsync(new Runnable() {

                @Override
                public void run() {
                    if (Boolean.getBoolean("SystemTray")
                            && !Boolean.getBoolean("ShowAlertVizBar")) {
                        if (amd.isText() == true) {
                            textBalloonMessage(statMsg, cat);
                        }
                    } else {
                        if (cat.getCategoryName().equals(Constants.MONITOR)
                                || (amd.isText() == true)) {
                            alertMessageDlg.messageHandler(statMsg, amd, cat,
                                    gConfig);
                        }

                        if (amd.isAudioEnabled() == true) {
                            alertMessageDlg.audioHandler(cat, gConfig);
                        }
                    }
                }

            });
        }

        // Play audio
        if (amd.isAudioEnabled() == true) {
            int textMsgBoxId = cat.getTextBox() - 1;
            // check for associated text message box; -1 is NONE
            if (textMsgBoxId >= 0) {
                audioMgr.stopTimer(textMsgBoxId);
                int durations = gConfig.getAudioDuration();
                String audioFile = amd.getAudioFile();
                if (audioFile != null) {
                    audioFile = getFullAudioFilePath(audioFile);
                } else {
                    audioFile = statMsg.getAudioFile();
                    if ((audioFile != null) && ((audioFile.trim().length() == 0)
                            || audioFile.equals("NONE"))) {
                        audioFile = null;
                    }
                    audioFile = getFullAudioFilePath(audioFile);
                }
                audioMgr.setAudioFile(audioFile, durations, textMsgBoxId);
                audioMgr.playLoopSound(textMsgBoxId);
            }
        }

        // Pop-up message
        if (amd.isPopup() == true) {
            if (alertPopupDlg == null) {
                alertPopupDlg = new AlertPopupMessageDlg(
                        alertMessageDlg.getShell(), statMsg,
                        gConfig.isExpandedPopup(), this, amd.getBackground());
                showPopup = true;
                ackAll = true;
                startBlinkTrayTimer();
            } else {
                alertPopupDlg.addNewMessage(statMsg, amd);
            }

            updateToolTip();
            if (doNotDisturb == false) {
                openAlertPopupDialog();
            }
        }
    }

    /**
     * Get full path name to a existing audio file.
     *
     * @param fname
     *            - name of audio file
     * @return filename when file found otherwise null
     */
    private String getFullAudioFilePath(String fname) {
        if (fname == null) {
            return null;
        }
        String filename = fname.trim();
        if (filename.isEmpty()) {
            return null;
        }
        File file = new File(filename);
        if (!file.exists()) {
            file = PathManagerFactory.getPathManager().getStaticFile(
                    LocalizationUtil.join("alertVizAudio", file.getName()));
            if ((file == null) || !file.exists()) {
                filename = null;
            } else {
                filename = file.getPath();
            }
        }
        return filename;
    }

    /**
     * Opens the alert pop-up dialog
     */
    public void openAlertPopupDialog() {
        if (alertPopupDlg != null) {
            alertPopupDlg.showDialog();
        } else {
            // should never happen
            Container.logInternal(Priority.ERROR,
                    "alertPopupDlg unexpectedly null");
        }
    }

    /**
     * Displays a pop-up balloon message
     *
     * @param statMsg
     *            Message to display.
     * @param cat
     *            Category of message.
     */
    private void textBalloonMessage(StatusMessage statMsg, Category cat) {

        if (toolTip != null) {
            toolTip.dispose();
        }

        toolTip = new ToolTip(alertMessageDlg.getShell(),
                SWT.BALLOON | SWT.ICON_WARNING);
        toolTip.setText(cat.getCategoryName());
        toolTip.setMessage(statMsg.getMessage());

        trayItem.setToolTip(toolTip);
        toolTip.setVisible(true);
    }

    /**
     * Adds a tool tip to the tray icon, if messages need to be acknowledged,
     * shows the number, otherwise displays general text.
     */
    private void updateToolTip() {
        if (alertPopupDlg == null) {
            this.trayItem.setToolTipText("AlertViz Menu");
        } else {
            int messages = alertPopupDlg.getNumberOfMessages();
            if (messages == 1) {
                this.trayItem.setToolTipText(
                        "There is " + messages + " message to be acknowledged");
            } else {
                this.trayItem.setToolTipText("There are " + messages
                        + " messages to be acknowledged");
            }
        }
    }

    /**
     * Cancels the blinking timer.
     */
    private void cancelTimer() {
        if (trayAlertTimer.timerIsRunning()) {
            trayAlertTimer.cancelTimer();
        }
    }

    /**
     * @return the exit status
     */
    public Integer getExitStatus() {
        return exitStatus;
    }

    /**
     * This is the button click event for the alertPopupDialog. This function is
     * called when "Hide Dialog" is clicked.
     */
    @Override
    public void handleEvent(Event event) {
        switch (event.type) {
        case SWT.Hide:
            ackAll = true;
            showPopup = true;
            startBlinkTrayTimer();
            updateToolTip();
            break;

        case SWT.Dispose:
            alertPopupDlg = null;
            cancelTimer();
            updateToolTip();

            ackAll = false;
            showPopup = false;
            break;
        default:
            Container.logInternal(Priority.WARN,
                    "Unexpected event type: " + event.type);
            break;
        }
    }

    @Override
    public void restart() {
        if (runningStandalone) {
            // Must use EXIT_RELAUNCH. EXIT_RESTART causes the
            // executable to do a restart without returning to
            // the shell/bat script. This fails. Any other value
            // such as Integer(1) the executable attempts to bring
            // up an error screen before exiting with the error code.
            exitStatus = IApplication.EXIT_RELAUNCH;
            this.dispose();
        }
    }

    @Override
    public void configurationChanged() {
        ConfigurationManager.getInstance().resetCustomLocalization();
        configData = ConfigurationManager.getInstance()
                .getCurrentConfiguration();
        configContext = ConfigurationManager.getInstance().getCurrentContext();
        if (alertMessageDlg != null) {
            alertMessageDlg.setConfigData(configData);
            if (configData.isMonitorLayoutChanged(prevConfigFile)) {
                if (alertMessageDlg.reLayout()) {
                    showAlertDlg = true;
                }
                prevConfigFile = configData.clone();
            }
            audioMgr = alertMessageDlg.getAlertAudioManager();
        }
    }

    /**
     * Get the number of text control composites.
     *
     * @return The number of text control composites.
     */
    private int getNumberOfTextControls() {
        int retVal = 1;

        TrayConfiguration.TrayMode layoutMode = configData
                .getGlobalConfiguration().getMode();

        switch (layoutMode) {
        case H1:
            retVal = 1;
            break;
        case V2:
        case H2:
            retVal = 2;
            break;
        case V3:
            retVal = 3;
            break;
        case V4:
        case Q4:
            retVal = 4;
            break;
        case MO:
            break;
        }

        return retVal;
    }
}
