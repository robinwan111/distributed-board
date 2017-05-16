package com.walidmoustafa.board.gui;/*
 * Name: Walid Moustafa
 * Student ID: 563080
 * Subject: COMP90015 - Distributed Systems
 * Assignment: Assignment 2 - Distributed Whiteboard
 * Project: com.walidmoustafa.board.App
 * File: com.walidmoustafa.board.gui.SharedPanel.java
*/

import com.walidmoustafa.board.App;

import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SharedPanel extends JPanel implements ActionListener, KeyListener {

    public static final int UNFILLED = 0;
    public static final int FILLED = 1;
    private static final long serialVersionUID = 1L;
    final int LINE = 0;
    final int RECT = 1;
    final int OVAL = 2;
    final int FREE = 3;
    final int TEXT = 4;
    BoardServer boardServer;
    String userID;
    JFrame mainFrame;
    ArrayList<Shape> shapes = new ArrayList<Shape>();
    Point startPoint = new Point(5, 10);
    int currentShape = LINE;
    int currentMode = UNFILLED;
    Color currentColor = getForeground();
    boolean erasing = false;
    int eraserSize = 1;
    ArrayList<Point> points;
    ArrayList<String> textInput;

    public SharedPanel(BoardServer bServer, String uID, JFrame frame) {
        boardServer = bServer;
        userID = uID;
        mainFrame = frame;
        MouseAdapter mouseAdapter = new MouseAdapter();
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addKeyListener(this);
        createMenuBar();
    }

    private void createMenuBar() {

        JMenuBar menuBar = new JMenuBar();


        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);

        if (App.isAdmin) {
            JMenuItem menuItem = new JMenuItem("New Board");
            menuItem.setActionCommand("NewBoard");
            menuItem.addActionListener(this);
            menuItem.setMnemonic(KeyEvent.VK_N);
            menu.add(menuItem);

            menuItem = new JMenuItem("Open File...");
            menuItem.setActionCommand("OpenFile");
            menuItem.addActionListener(this);
            menuItem.setMnemonic(KeyEvent.VK_O);
            menu.add(menuItem);

            menu.addSeparator();

            menuItem = new JMenuItem("Save As...");
            menuItem.setActionCommand("SaveAs");
            menuItem.addActionListener(this);
            menuItem.setMnemonic(KeyEvent.VK_S);
            menu.add(menuItem);

            menu.addSeparator();
        }

        menu.add(new AbstractAction("Exit") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (mainFrame.isActive()) {
                    WindowEvent windowClosing = new WindowEvent(mainFrame,
                            WindowEvent.WINDOW_CLOSING);
                    mainFrame.dispatchEvent(windowClosing);
                }
            }
        });

        menuBar.add(menu);

        menu = new JMenu("Shapes");
        menu.setMnemonic(KeyEvent.VK_S);
        JRadioButtonMenuItem rbMenuItem;

        ButtonGroup group = new ButtonGroup();

        rbMenuItem = new JRadioButtonMenuItem("com.walidmoustafa.board.gui.Line");
        rbMenuItem.setActionCommand("com.walidmoustafa.board.gui.Line");
        rbMenuItem.addActionListener(this);
        rbMenuItem.setSelected(true);
        rbMenuItem.setMnemonic(KeyEvent.VK_L);
        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("Rectangle");
        rbMenuItem.setActionCommand("Rectangle");
        rbMenuItem.addActionListener(this);
        rbMenuItem.setMnemonic(KeyEvent.VK_R);
        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("com.walidmoustafa.board.gui.Oval");
        rbMenuItem.setActionCommand("com.walidmoustafa.board.gui.Oval");
        rbMenuItem.addActionListener(this);
        rbMenuItem.setMnemonic(KeyEvent.VK_O);
        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("Free Hand");
        rbMenuItem.setActionCommand("Free Hand");
        rbMenuItem.addActionListener(this);
        rbMenuItem.setMnemonic(KeyEvent.VK_F);
        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("com.walidmoustafa.board.gui.Text");
        rbMenuItem.setActionCommand("com.walidmoustafa.board.gui.Text");
        rbMenuItem.addActionListener(this);
        rbMenuItem.setMnemonic(KeyEvent.VK_T);
        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        menuBar.add(menu);

        JMenu menuMode = new JMenu("Mode");
        menuMode.setMnemonic(KeyEvent.VK_M);

        group = new ButtonGroup();

        rbMenuItem = new JRadioButtonMenuItem("Unfilled");
        rbMenuItem.setActionCommand("Unfilled");
        rbMenuItem.addActionListener(this);
        rbMenuItem.setSelected(true);
        rbMenuItem.setMnemonic(KeyEvent.VK_U);
        group.add(rbMenuItem);
        menuMode.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("Filled");
        rbMenuItem.setActionCommand("Filled");
        rbMenuItem.addActionListener(this);
        rbMenuItem.setMnemonic(KeyEvent.VK_F);
        group.add(rbMenuItem);
        menuMode.add(rbMenuItem);

        menuMode.addSeparator();

        JMenuItem menuItem = new JMenuItem("Colors");
        menuItem.setActionCommand("Colors");
        menuItem.addActionListener(this);
        menuItem.setMnemonic(KeyEvent.VK_C);
        menuMode.add(menuItem);

        menuBar.add(menuMode);

        JMenu menuEraser = new JMenu("com.walidmoustafa.board.gui.Eraser");
        menuEraser.setMnemonic(KeyEvent.VK_E);
        JCheckBoxMenuItem cbMenuItem;

        cbMenuItem = new JCheckBoxMenuItem("Erase");
        cbMenuItem.setActionCommand("ToggleErase");
        cbMenuItem.addActionListener(this);
        cbMenuItem.setMnemonic(KeyEvent.VK_E);
        menuEraser.add(cbMenuItem);

        menuEraser.addSeparator();

        group = new ButtonGroup();

        rbMenuItem = new JRadioButtonMenuItem("Small");
        rbMenuItem.setActionCommand("Small");
        rbMenuItem.addActionListener(this);
        rbMenuItem.setSelected(true);
        rbMenuItem.setMnemonic(KeyEvent.VK_S);
        group.add(rbMenuItem);
        menuEraser.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("Medium");
        rbMenuItem.setActionCommand("Medium");
        rbMenuItem.addActionListener(this);
        rbMenuItem.setSelected(true);
        rbMenuItem.setMnemonic(KeyEvent.VK_M);
        group.add(rbMenuItem);
        menuEraser.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("Large");
        rbMenuItem.setActionCommand("Large");
        rbMenuItem.addActionListener(this);
        rbMenuItem.setSelected(true);
        rbMenuItem.setMnemonic(KeyEvent.VK_M);
        group.add(rbMenuItem);
        menuEraser.add(rbMenuItem);

        menuBar.add(menuEraser);

        menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);

        menuItem = new JMenuItem("About...");
        menuItem.setActionCommand("About");
        menuItem.addActionListener(this);
        menuItem.setMnemonic(KeyEvent.VK_A);
        menu.add(menuItem);

        menuBar.add(menu);

        mainFrame.setJMenuBar(menuBar);
    }

    public synchronized void addShape(Shape shape) {
        shapes.add(shape);
    }

    @Override
    public synchronized void paintComponent(Graphics gfx) {
        for (Shape shape : shapes) {
            shape.draw(gfx);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.equals("NewBoard")) {
            newBoard();
        } else if (action.equals("OpenFile")) {
            loadBoard();
        } else if (action.equals("SaveAs")) {
            saveBoard();
        } else if (action.equals("com.walidmoustafa.board.gui.Line")) {
            currentShape = LINE;
        } else if (action.equals("Rectangle")) {
            currentShape = RECT;
        } else if (action.equals("com.walidmoustafa.board.gui.Oval")) {
            currentShape = OVAL;
        } else if (action.equals("Free Hand")) {
            currentShape = FREE;
        } else if (action.equals("com.walidmoustafa.board.gui.Text")) {
            currentShape = TEXT;
        } else if (action.equals("Colors")) {
            currentColor = JColorChooser.showDialog(this, "Fill Color...", getForeground());
        } else if (action.equals("Unfilled")) {
            currentMode = UNFILLED;
        } else if (action.equals("Filled")) {
            currentMode = FILLED;
        } else if (action.equals("ToggleErase")) {
            erasing = !erasing;
        } else if (action.equals("Small")) {
            eraserSize = 1;
        } else if (action.equals("Medium")) {
            eraserSize = 2;
        } else if (action.equals("Large")) {
            eraserSize = 3;
        } else if (action.equals("About")) {
            aboutApp();
        }
    }

    public void newBoard() {
        BoardEvent event = new BoardEvent("loadBoard");
        event.shapes = new ArrayList<Shape>();
        try {
            boardServer.addBoardEvent(event);
        } catch (RemoteException e1) {
            e1.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void loadBoard() {
        JFileChooser chooser = new JFileChooser();
        FileFilter[] filefilters = chooser.getChoosableFileFilters();
        for (FileFilter filter : filefilters) {
            chooser.removeChoosableFileFilter(filter);
        }
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Board Files (*.brd)", "brd");
        chooser.setFileFilter(filter);

        int returnVal = chooser.showOpenDialog(mainFrame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                ObjectInputStream is = new ObjectInputStream(new FileInputStream(
                        chooser.getSelectedFile()));
                Object board = is.readObject();
                if (board instanceof ArrayList<?>) {
                    BoardEvent event = new BoardEvent("loadBoard");
                    event.shapes = (ArrayList<Shape>) board;
                    try {
                        boardServer.addBoardEvent(event);
                    } catch (RemoteException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "Corrupted file contents",
                            "Corrupted File", JOptionPane.INFORMATION_MESSAGE);
                }
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void saveBoard() {
        JFileChooser chooser = new JFileChooser();
        FileFilter[] filefilters = chooser.getChoosableFileFilters();
        for (FileFilter filter : filefilters) {
            chooser.removeChoosableFileFilter(filter);
        }
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Board Files (*.brd)", "brd");
        chooser.setFileFilter(filter);

        int returnVal = chooser.showSaveDialog(mainFrame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(
                        chooser.getSelectedFile()));
                os.writeObject(shapes);
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        keyTyping(e);
    }

    public void keyTyping(KeyEvent e) {
        if (currentShape == TEXT) {
            if (textInput == null) {
                textInput = new ArrayList<String>();
                textInput.add(new String());
            }

            char c = e.getKeyChar();
            if (c == '\n') {
                textInput.add(new String());
            } else {
                int lastLine = textInput.size() - 1;
                String lastStr = textInput.get(lastLine);
                textInput.set(lastLine, lastStr + c);
            }
            //send to server
            BoardEvent event = new BoardEvent("keyTyped");
            event.startPoint = startPoint;
            event.textInput = textInput;
            try {
                boardServer.addBoardEvent(event);
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public void aboutApp() {
        JOptionPane.showMessageDialog(mainFrame, "Distributed Board\nBy: Walid Moustafa\nMIT\nMelbourne University",
                "About Shared Board", JOptionPane.INFORMATION_MESSAGE);
    }

    class MouseAdapter extends MouseInputAdapter {

        @Override
        public void mouseMoved(MouseEvent e) {
            if ((textInput != null) && (!textInput.isEmpty())) {
                //send to server
                BoardEvent event = new BoardEvent("mouseMoved");
                event.startPoint = startPoint;
                event.textInput = textInput;
                try {
                    boardServer.addBoardEvent(event);
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
                //
                int spacing = getGraphics().getFontMetrics().getHeight();
                startPoint = new Point(startPoint.x, startPoint.y + spacing);
                textInput = null;
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            startPoint = e.getPoint();
            if (erasing || (currentShape == FREE)) {
                points = new ArrayList<Point>();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            Point endPoint = e.getPoint();

            if (erasing || (currentShape == FREE)) {
                points.add(new Point(e.getX(), e.getY()));
            }

            //send to server
            BoardEvent event = new BoardEvent("mouseDragged");
            event.currentShape = currentShape;
            event.currentMode = currentMode;
            event.currentColor = currentColor;
            event.erasing = erasing;
            event.eraserSize = eraserSize;
            event.startPoint = startPoint;
            event.endPoint = endPoint;
            event.points = points;
            try {
                boardServer.addBoardEvent(event);
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            Point endPoint = e.getPoint();

            //send to server
            BoardEvent event = new BoardEvent("mouseReleased");
            event.currentShape = currentShape;
            event.currentMode = currentMode;
            event.currentColor = currentColor;
            event.erasing = erasing;
            event.eraserSize = eraserSize;
            event.startPoint = startPoint;
            event.endPoint = endPoint;
            event.points = points;
            try {
                boardServer.addBoardEvent(event);
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
        }
    }
}