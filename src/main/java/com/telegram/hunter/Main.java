package com.telegram.hunter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TelegramClient;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.google.common.collect.Lists;
import com.telegram.hunter.dto.KnifeDTO;
import com.telegram.hunter.dto.LoginDTO;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.NumberUtil;

public class Main {
	
	public static Display display;
	public static Shell shell;
	
	private static final LoginDTO defaultLoginDTO = new LoginDTO();
	
	static {
		try {
			List<String> lines = FileUtil.readLines("C:/tdlib-db/login.txt", StandardCharsets.UTF_8);
			if (CollectionUtils.isNotEmpty(lines) && lines.size() >= 3) {
				defaultLoginDTO.setApiId(StringUtils.trim(lines.get(0)));
				defaultLoginDTO.setApiHash(StringUtils.trim(lines.get(1)));
				defaultLoginDTO.setPhone(StringUtils.trim(lines.get(2)));
			}
		} catch (IORuntimeException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		Main.display = display;
		Main.shell = shell;
		
		shell.setText("机器人");
		shell.setSize(1600, 880);
		shell.setLayout(new GridLayout(2, false));
		((GridLayout) shell.getLayout()).verticalSpacing = 15;
		
		// 设置窗口位置
		Rectangle screenSize = display.getBounds();
		Rectangle shellSize = shell.getBounds();
		int x = (screenSize.width - shellSize.width) / 2;
		int y = (screenSize.height - shellSize.height) / 2;
		shell.setLocation(x, y);
		
		CTabFolder tabFolder = new CTabFolder(shell, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		// 添加选项卡选中事件
		tabFolder.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			
			@Override
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				for (CTabItem item : tabFolder.getItems()) {
					if (item == tabFolder.getSelection()) {
						Font boldFont = new Font(display, item.getFont().getFontData()[0].getName(), item.getFont().getFontData()[0].getHeight(), SWT.BOLD);
						item.setFont(boldFont);
					} else {
						Font normalFont = new Font(display, item.getFont().getFontData()[0].getName(), item.getFont().getFontData()[0].getHeight(), SWT.NORMAL);
						item.setFont(normalFont);
					}
				}
			}
		});
		
		// Telegram
		swtTelegram(display, shell, tabFolder);
		
		// 刮刀
		swtKnife(display, shell, tabFolder);
		
		// 日志
        Text outputText = new Text(shell, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		GridData outputData = new GridData(SWT.FILL, SWT.TOP, true, false);
		outputData.heightHint = 800;
		outputData.widthHint = 600;
		outputText.setLayoutData(outputData);
		outputText.setEditable(false);
		
		// 添加鼠标右键清除
		Menu menu = new Menu(outputText);
		MenuItem clearItem = new MenuItem(menu, SWT.PUSH);
		clearItem.setText("清除");
		clearItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				outputText.setText("");
			}
		});
		outputText.setMenu(menu);
		
		// 输出System.out.println内容
		asyncRedirectSystemOut(display, outputText);
		
        //shell.pack();
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
        System.exit(0);
	}
	
	/**
	 * 刮刀
	 * @param display
	 * @param shell
	 * @param tabFolder
	 */
	private static void swtKnife(Display display, Shell shell, CTabFolder tabFolder) {
		CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
		tabItem.setText(" 刮刀 ");
		
		Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		tabItem.setControl(composite);
		
        // 群组
        Label groupLabel = new Label(composite, SWT.NONE);
        groupLabel.setText("群组");
        groupLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
        StyledText groupText = new StyledText(composite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        GridData groupGridData = new GridData(GridData.FILL_HORIZONTAL);
        groupGridData.widthHint = 500;
        groupGridData.heightHint = 120;
        groupText.setLayoutData(groupGridData);
        
        // 用户
        Label userLabel = new Label(composite, SWT.NONE);
        userLabel.setText("用户");
        userLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
        StyledText userText = new StyledText(composite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        GridData userGridData = new GridData(GridData.FILL_HORIZONTAL);
        userGridData.widthHint = 500;
        userGridData.heightHint = 120;
        userText.setLayoutData(userGridData);
        
        // 转发
        Label destLabel = new Label(composite, SWT.NONE);
        destLabel.setText("转发");
        destLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
        Text destText = new Text(composite, SWT.BORDER);
        destText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        // 时间间隔
        Label intervalLabel = new Label(composite, SWT.NONE);
        intervalLabel.setText("间隔(秒)");
        intervalLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
        Text intervalText = new Text(composite, SWT.BORDER);
        intervalText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        intervalText.setText("5");
        
        // 刮刀
        Button submitButton = new Button(composite, SWT.PUSH);
        submitButton.setText("开始刮刀");
        GridData submitButtonData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        submitButtonData.horizontalSpan = 2;
        submitButtonData.heightHint = 45;
        submitButton.setLayoutData(submitButtonData);
        
        groupLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				InputDialog dialog = new InputDialog(Main.shell, "", "请输入群组ID", "", null);
				if (dialog.open() == InputDialog.OK) {
					String groupId = dialog.getValue();
					if (StringUtils.isNotBlank(groupId)) {
		                TelegramClient.getChatAdministrators(groupId);
					}
				}
			}
		});
        
        userLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				InputDialog dialog = new InputDialog(Main.shell, "", "请输入用户名", "", null);
				if (dialog.open() == InputDialog.OK) {
					String username = dialog.getValue();
					if (StringUtils.isNotBlank(username)) {
		                TelegramClient.getUserIdByUsername(username);
					}
				}
			}
		});
        
        submitButton.addListener(SWT.Selection, event -> {
        	String text = submitButton.getText();
        	switch (text) {
				case "开始刮刀":
					// 群组
					List<String> groups = Lists.newArrayList();
					for (int i = 0; i < groupText.getLineCount(); i++) {
						String line = StringUtils.trim(groupText.getLine(i));
						if (StringUtils.isNotBlank(line)) {
							groups.add(line);
						}
					}
					// 用户
					List<String> users = Lists.newArrayList();
					for (int i = 0; i < userText.getLineCount(); i++) {
						String line = StringUtils.trim(userText.getLine(i));
						if (StringUtils.isNotBlank(line)) {
							users.add(line);
						}
					}
					
					KnifeDTO knifeDTO = new KnifeDTO();
					knifeDTO.setGroups(groups);
					knifeDTO.setUsers(users);
					knifeDTO.setDest(StringUtils.trim(destText.getText()));
					knifeDTO.setInterval(StringUtils.trim(intervalText.getText()));
					
					// 表单校验
					if (!checkForm(shell, knifeDTO)) {
						return;
					}
					
					submitButton.setText("停止刮刀");
					new Thread(() -> {
						TelegramClient.startKnife = true;
						try {
							TelegramClient.startKnife(knifeDTO);
						} finally {
							display.asyncExec(() -> {
								submitButton.setText("开始刮刀");
								TelegramClient.startKnife = false;
							});
						}
					}).start();
					break;
				case "停止刮刀":
					submitButton.setText("开始刮刀");
					TelegramClient.startKnife = false;
					break;
				default:
					break;
			}
        });
	}
	
	/**
	 * 表单校验
	 * @param shell
	 * @param knifeDTO
	 */
	private static boolean checkForm(Shell shell, KnifeDTO knifeDTO) {
		if (CollectionUtils.isEmpty(knifeDTO.getGroups())) {
			alert(shell, SWT.ICON_ERROR, "群组不能为空");
			return false;
		} else {
			if (knifeDTO.getGroups().size() > 10) {
				alert(shell, SWT.ICON_ERROR, "群组数量最大限制10个");
				return false;
			}
		}
		
		if (StringUtils.isBlank(knifeDTO.getDest())) {
			alert(shell, SWT.ICON_ERROR, "转发不能为空");
			return false;
		}
		
		if (StringUtils.isBlank(knifeDTO.getInterval())) {
			alert(shell, SWT.ICON_ERROR, "刮刀间隔不能为空");
			return false;
		} else {
			if (!NumberUtil.isInteger(knifeDTO.getInterval())) {
				alert(shell, SWT.ICON_ERROR, "刮刀间隔格式错误");
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Telegram
	 * @param display
	 * @param shell
	 * @param tabFolder
	 */
	private static void swtTelegram(Display display, Shell shell, CTabFolder tabFolder) {
		CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
		tabItem.setText(" 电报 ");
		
		Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		tabItem.setControl(composite);
		
        // apiId
        Label apiIdLabel = new Label(composite, SWT.NONE);
        apiIdLabel.setText("apiId");
        apiIdLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
        Text apiIdText = new Text(composite, SWT.BORDER);
        GridData apiIdGridData = new GridData(GridData.FILL_HORIZONTAL);
        apiIdGridData.widthHint = 500;
        apiIdText.setLayoutData(apiIdGridData);
        apiIdText.setLayoutData(apiIdGridData);
        apiIdText.setText(StringUtils.defaultString(defaultLoginDTO.getApiId()));
        
        // apiHash
        Label apiHashLabel = new Label(composite, SWT.NONE);
        apiHashLabel.setText("apiHash");
        apiHashLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
        Text apiHashText = new Text(composite, SWT.BORDER);
        apiHashText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        apiHashText.setText(StringUtils.defaultString(defaultLoginDTO.getApiHash()));
        
        // phone
        Label phoneLabel = new Label(composite, SWT.NONE);
        phoneLabel.setText("phone");
        phoneLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
        Text phoneText = new Text(composite, SWT.BORDER);
        phoneText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        phoneText.setText(StringUtils.defaultString(defaultLoginDTO.getPhone()));
        
        // 复选框
        Composite checkboxContainer = new Composite(composite, SWT.NONE);
        checkboxContainer.setLayout(new GridLayout(5, false));
        GridData checkboxGridData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        checkboxGridData.horizontalSpan = 2;
        checkboxContainer.setLayoutData(checkboxGridData);
        
        Button useProxyCheckbox = new Button(checkboxContainer, SWT.CHECK);
        useProxyCheckbox.setText("使用代理");
        Label blankLabel = new Label(checkboxContainer, SWT.NONE);
        blankLabel.setText("	");
        Button onlyCa = new Button(checkboxContainer, SWT.CHECK);
        onlyCa.setText("刮合约");
        
        // Login
        Button submitButton = new Button(composite, SWT.PUSH);
        submitButton.setText("登录");
        GridData submitButtonData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        submitButtonData.horizontalSpan = 2;
        submitButtonData.heightHint = 45;
        submitButton.setLayoutData(submitButtonData);
        
        useProxyCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	Client.useProxy = useProxyCheckbox.getSelection();
            }
        });
        
        onlyCa.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	TelegramClient.onlyCa = onlyCa.getSelection();
            }
        });
        
        submitButton.addListener(SWT.Selection, event -> {
        	String text = submitButton.getText();
        	switch (text) {
				case "登录":
					LoginDTO loginDTO = new LoginDTO();
					loginDTO.setApiId(StringUtils.trim(apiIdText.getText()));
					loginDTO.setApiHash(StringUtils.trim(apiHashText.getText()));
					loginDTO.setPhone(StringUtils.trim(phoneText.getText()));
					
					// 表单校验
					if (!checkForm(shell, loginDTO)) {
						return;
					}
					
					submitButton.setText("登录中");
					
					new Thread(() -> {
						try {
							TelegramClient.startup(shell, Integer.parseInt(loginDTO.getApiId()), loginDTO.getApiHash(), loginDTO.getPhone());
					} finally {
							display.asyncExec(() -> {
								submitButton.setText("登录成功");
								submitButton.setEnabled(false);
							});
						}
					}).start();
					break;
				default:
					break;
			}
        });
	}
	
	/**
	 * 表单校验
	 * @param shell
	 * @param loginDTO
	 */
	private static boolean checkForm(Shell shell, LoginDTO loginDTO) {
		if (StringUtils.isBlank(loginDTO.getApiId())) {
			alert(shell, SWT.ICON_ERROR, "apiId不能为空");
			return false;
		}
		if (StringUtils.isBlank(loginDTO.getApiHash())) {
			alert(shell, SWT.ICON_ERROR, "apiHash不能为空");
			return false;
		}
		if (StringUtils.isBlank(loginDTO.getPhone())) {
			alert(shell, SWT.ICON_ERROR, "phone不能为空");
			return false;
		}
		return true;
	}
	
	/**
	 * 弹框提示
	 * @param shell
	 * @param type
	 * @param message
	 */
	private static void alert(Shell shell, int type, String message) {
		MessageBox messageBox = new MessageBox(shell, SWT.OK | type);
        messageBox.setMessage(message);
        messageBox.open();
	}
	
	/**
	 * 输出System.out.println内容
	 * @param display
	 * @param outputText
	 */
	private static void asyncRedirectSystemOut(Display display, Text outputText) {
		new Thread(() -> redirectSystemOut(display, outputText)).start();
	}
	
	private static void redirectSystemOut(Display display, Text outputText) {
		PipedOutputStream pipedOutputStream = null;
		PipedInputStream pipedInputStream = null;
		try {
			pipedOutputStream = new PipedOutputStream();
			System.setOut(new PrintStream(pipedOutputStream, true));

			pipedInputStream = new PipedInputStream(pipedOutputStream);
			try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(pipedInputStream))) {
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					String str = line;
					display.asyncExec(() -> {
						if (!outputText.isDisposed()) {
							outputText.append(str + System.getProperty("line.separator"));
						}
					});
				}
			}
		} catch (IOException e) {
			redirectSystemOut(display, outputText);
		} finally {
			try {
				if (pipedOutputStream != null) {
					pipedOutputStream.close();
				}
				if (pipedInputStream != null) {
					pipedInputStream.close();
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
}
