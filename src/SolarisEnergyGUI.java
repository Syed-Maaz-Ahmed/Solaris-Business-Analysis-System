// Solaris Elite // Design Standard 7.0 (Custom Portal Core)
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class SolarisEnergyGUI extends JFrame {
    private SolarisEnergyBackend backend;
    private JPanel mainArea;
    private CardLayout flow;
    
    // Industrial Master Palette
    private final Color CLR_VOID = new Color(5, 8, 15);
    private final Color CLR_GLASS = new Color(20, 28, 48, 245);
    private final Color ACC_CYAN = new Color(0, 212, 255);
    private final Color ACC_GREEN = new Color(0, 255, 120);
    private final Color ACC_GOLD = new Color(255, 180, 0);
    private final Color TXT_PRI = new Color(248, 250, 255);
    private final Color TXT_DARK = new Color(130, 145, 175);
    private final Color BRD_COL = new Color(255, 255, 255, 12);

    private List<NavBtn> dashLinks = new ArrayList<>();

    public SolarisEnergyGUI() {
        backend = new SolarisEnergyBackend();
        configureApp();
    }

    private void configureApp() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        setTitle("SOLARIS // MISSION CONTROL GLOBAL");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1500, 950));
        
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setPaint(new GradientPaint(0,0, CLR_VOID, getWidth(), getHeight(), new Color(15, 22, 38)));
                g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        setContentPane(root);

        add(createSidebar(), BorderLayout.WEST);

        flow = new CardLayout();
        mainArea = new JPanel(flow) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setPaint(new GradientPaint(0,0, CLR_VOID, getWidth(), getHeight(), new Color(15, 22, 38)));
                g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        mainArea.setOpaque(true);
        mainArea.setBorder(new EmptyBorder(50, 65, 50, 65));

        mainArea.add(pageDash(), "dashboard");
        mainArea.add(pageInv(), "inventory");
        mainArea.add(pageOrd(), "orders");
        mainArea.add(pageRev(), "sales");

        add(mainArea, BorderLayout.CENTER);
        sync();
        pack(); setLocationRelativeTo(null);
    }

    private JPanel createSidebar() {
        JPanel s = new JPanel(new BorderLayout()); s.setOpaque(false); s.setPreferredSize(new Dimension(280, 0));
        s.setBorder(new MatteBorder(0, 0, 0, 1, BRD_COL));
        
        JPanel top = new JPanel(); top.setOpaque(false); 
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(new EmptyBorder(50, 20, 0, 10));
        
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0)); brand.setOpaque(false);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT); brand.add(new VectorLogo());
        JLabel n = new JLabel("SOLARIS"); n.setFont(new Font("Segoe UI", Font.BOLD, 22)); n.setForeground(TXT_PRI);
        brand.add(n); top.add(brand);
        top.add(Box.createVerticalStrut(50));

        String[] labels = {"Dashboard", "Inventory", "Orders", "Sales"};
        for(String l : labels) {
            NavBtn b = new NavBtn(l); b.setAlignmentX(Component.LEFT_ALIGNMENT);
            top.add(b); top.add(Box.createVerticalStrut(10));
            dashLinks.add(b); if(l.equals("Dashboard")) b.setActive(true);
        }
        s.add(top, BorderLayout.NORTH);

        JPanel bot = new JPanel(new BorderLayout()); bot.setOpaque(false); bot.setBorder(new EmptyBorder(0, 20, 40, 20));
        bot.add(styledBtn("SYNC STATUS", ACC_CYAN, e -> { backend.save(); sync(); }));
        s.add(bot, BorderLayout.SOUTH);
        return s;
    }

    private class VectorLogo extends JPanel {
        public VectorLogo() { setOpaque(false); setPreferredSize(new Dimension(35, 45)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Path2D.Double bolt = new Path2D.Double();
            bolt.moveTo(18, 5); bolt.lineTo(30, 5); bolt.lineTo(12, 22); bolt.lineTo(22, 22); bolt.lineTo(5, 40); bolt.lineTo(22, 17); bolt.lineTo(12, 17); bolt.closePath();
            g2.setColor(new Color(ACC_CYAN.getRed(), ACC_CYAN.getGreen(), ACC_CYAN.getBlue(), 60));
            g2.setStroke(new BasicStroke(5)); g2.draw(bolt);
            g2.setPaint(new GradientPaint(0,0,ACC_CYAN, 0, getHeight(), ACC_GREEN)); g2.fill(bolt);
            g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(0.8f)); g2.draw(bolt);
            g2.dispose();
        }
    }

    private class NavBtn extends JButton {
        private boolean active = false;
        public NavBtn(String t) {
            super(t); setFont(new Font("Segoe UI", Font.BOLD, 18)); setForeground(TXT_DARK);
            setFocusPainted(false); setBorderPainted(false); setContentAreaFilled(false);
            setHorizontalAlignment(SwingConstants.LEFT); setMaximumSize(new Dimension(260, 80));
            setCursor(new Cursor(Cursor.HAND_CURSOR)); setBorder(new EmptyBorder(0, 28, 0, 0));
            addActionListener(e -> {
                flow.show(mainArea, t.toLowerCase());
                for(NavBtn c : dashLinks) c.setActive(c.getText().equals(t));
                sync();
            });
        }
        public void setActive(boolean a) { this.active = a; setForeground(a?TXT_PRI:TXT_DARK); repaint(); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(active) {
                Graphics2D g2 = (Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 12)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                int indH = (int)(getHeight() * 0.75); int indY = (getHeight() - indH) / 2;
                g2.setColor(ACC_GREEN); g2.fillRoundRect(0, indY, 6, indH, 6, 6); g2.dispose();
            }
        }
    }

    private StatBox c1, c2;
    private DefaultTableModel mP, mI, mS;
    private TrendPlot plot;
    private StockDonut donut;
    private JPanel alertRack;
    private JPanel legendRack;

    private JPanel pageDash() {
        JPanel p = new JPanel(new BorderLayout(0, 50)); 
        p.setBackground(new Color(15, 22, 40)); p.setOpaque(true);
        p.add(topHead("Grid Logistics", "Real-time energy distribution & mission telemetry"), BorderLayout.NORTH);
        JPanel mid = new JPanel(new GridLayout(1, 2, 45, 0)); mid.setOpaque(false);
        c1 = new StatBox("CUMULATIVE REVENUE", "$0.00", ACC_GREEN);
        c2 = new StatBox("ACTIVE MISSIONS", "0", ACC_CYAN);
        mid.add(c1); mid.add(c2); p.add(mid, BorderLayout.CENTER);
        JPanel bot = new JPanel(new BorderLayout(0, 20)); bot.setOpaque(false); bot.setPreferredSize(new Dimension(0, 320));
        JLabel l = new JLabel("INVENTORY DEFICIT MONITOR"); l.setFont(new Font("Segoe UI", Font.BOLD, 12)); l.setForeground(TXT_DARK);
        bot.add(l, BorderLayout.NORTH);
        alertRack = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0)); alertRack.setOpaque(false);
        JScrollPane sc = new JScrollPane(alertRack); sc.setOpaque(false); sc.getViewport().setOpaque(false); sc.setBorder(null);
        sc.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        bot.add(sc, BorderLayout.CENTER); p.add(bot, BorderLayout.SOUTH);
        return p;
    }

    private JPanel pageInv() {
        JPanel p = new JPanel(new BorderLayout(0, 40)); 
        p.setBackground(new Color(15, 22, 40)); p.setOpaque(true);
        p.add(topHead("Hardware Hub", "Central unit storage and valuation matrix"), BorderLayout.NORTH);
        JPanel m = new JPanel(new BorderLayout(45,0)); m.setOpaque(false);
        mI = new DefaultTableModel(new String[]{"ITEM DESIGNATION", "VALUATION", "STOCK LEVEL"}, 0);
        JTable table = buildTable(mI, new int[]{450, 200, 150}); m.add(wrapTable(table), BorderLayout.CENTER);
        JPanel sid = new JPanel(new BorderLayout(0, 25)); sid.setOpaque(false); sid.setPreferredSize(new Dimension(420, 0));
        donut = new StockDonut(); donut.setPreferredSize(new Dimension(420, 380)); sid.add(donut, BorderLayout.NORTH);
        legendRack = new JPanel(); legendRack.setOpaque(false); legendRack.setLayout(new BoxLayout(legendRack, BoxLayout.Y_AXIS));
        sid.add(legendRack, BorderLayout.CENTER); m.add(sid, BorderLayout.EAST);
        p.add(m, BorderLayout.CENTER);
        JPanel b = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0)); b.setOpaque(false);
        b.add(styledBtn("UPDATE STOCK", ACC_CYAN, e -> updateInv()));
        b.add(styledBtn("+ ADD REQUISITION", ACC_GREEN, e -> addInv())); p.add(b, BorderLayout.SOUTH);
        return p;
    }

    private JPanel pageOrd() {
        JPanel p = new JPanel(new BorderLayout(0, 45)); 
        p.setBackground(new Color(15, 22, 40)); p.setOpaque(true);
        p.add(topHead("Mission Dispatch", "Active hardware deployment logistics track"), BorderLayout.NORTH);
        
        mP = new DefaultTableModel(new String[]{"ID", "CLIENT NAME", "LOCATION SITE", "STATUS", "REVENUE"}, 0);
        JTable table = buildTable(mP, new int[]{100, 300, 300, 150, 150});
        p.add(wrapTable(table), BorderLayout.CENTER);
        
        JPanel b = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0)); b.setOpaque(false);
        b.add(styledBtn("INITIATE MISSION", ACC_GREEN, e -> addOrd())); 
        b.add(styledBtn("EXECUTE DISPATCH", ACC_CYAN, e -> disp()));
        
        p.add(b, BorderLayout.SOUTH); 
        return p;
    }

    private JPanel pageRev() {
        JPanel p = new JPanel(new BorderLayout(0, 40)); 
        p.setBackground(new Color(15, 22, 40)); p.setOpaque(true);
        p.add(topHead("Revenue Stream", "Enterprise mission performance analytics"), BorderLayout.NORTH);
        plot = new TrendPlot(); p.add(plot, BorderLayout.CENTER);
        mS = new DefaultTableModel(new String[]{"ID", "CLIENT NAME", "MODEL", "FINAL VALUE", "TIMESTAMP"}, 0);
        JTable table = buildTable(mS, new int[]{120, 250, 400, 200, 150});
        JPanel tArea = wrapTable(table); tArea.setPreferredSize(new Dimension(0, 350)); p.add(tArea, BorderLayout.SOUTH);
        return p;
    }

    private void sync() {
        if(c1 != null) {
            double total = backend.getRequests().getTotalRevenue();
            c1.update("$ " + String.format("%,.2f", total));
            c2.update(String.valueOf(backend.getRequests().getActive().size()));
            mI.setRowCount(0); for(EnergyAsset a : backend.getInventory().getItems()) mI.addRow(new Object[]{a.getName().toUpperCase(), "$ "+a.getPrice(), (int)a.getQuantity() + " Units"});
            mP.setRowCount(0); for(DeploymentOrder o : backend.getRequests().getActive()) mP.addRow(new Object[]{"S-"+o.getOrderId(), o.getClientName().toUpperCase(), o.getClientLocation(), o.getStatus().toUpperCase(), "$"+o.getTotalRevenue()});
            mS.setRowCount(0); for(DeploymentOrder o : backend.getRequests().getHistory()) mS.addRow(new Object[]{"S-"+o.getOrderId(), o.getClientName().toUpperCase(), o.getAssetName(), "$"+o.getTotalRevenue(), o.getDate().toString().substring(5,16)});
            List<Double> data = new ArrayList<>(); for(DeploymentOrder o : backend.getRequests().getHistory()) data.add(o.getTotalRevenue());
            plot.setData(data); donut.setData(backend.getInventory().getItems());
            legendRack.removeAll(); Color[] clrs = {ACC_CYAN, ACC_GREEN, ACC_GOLD, Color.MAGENTA, Color.ORANGE};
            int i=0; for(EnergyAsset a : backend.getInventory().getItems()){ legendRack.add(legendLine(a.getName().toUpperCase(), clrs[i%clrs.length])); i++; if(i>=10) break;}
            legendRack.revalidate(); legendRack.repaint();
            alertRack.removeAll(); boolean low = false;
            for(EnergyAsset a : backend.getInventory().getItems()){ if(a.getQuantity() <= 5) { alertRack.add(new AlertTile(a.getName(), (int)a.getQuantity())); low = true; } }
            if(!low) { 
                JPanel e = new GlassBox(); e.setPreferredSize(new Dimension(550, 160)); e.setLayout(new BorderLayout()); e.setBorder(new EmptyBorder(0,40,0,40));
                JLabel t = new JLabel("GRID STABLE // NO ACTIVE DEFICITS"); t.setForeground(ACC_GREEN); t.setFont(new Font("Segoe UI", Font.BOLD, 14)); e.add(t); alertRack.add(e);
            }
            alertRack.revalidate(); alertRack.repaint();
            mainArea.revalidate(); mainArea.repaint();
        }
    }

    private JPanel legendLine(String t, Color c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 6)); p.setOpaque(false);
        JPanel dot = new JPanel() { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(c); g2.fillOval(0,0,10,10); g2.dispose(); } };
        dot.setPreferredSize(new Dimension(10, 10)); p.add(dot);
        JLabel l = new JLabel(t); l.setForeground(TXT_DARK); l.setFont(new Font("Segoe UI", Font.BOLD, 11)); p.add(l);
        return p;
    }

    private class StatBox extends GlassBox {
        private JLabel v;
        public StatBox(String l, String val, Color c) {
            setLayout(new BorderLayout()); setBorder(new EmptyBorder(35, 45, 35, 45));
            JLabel lbl = new JLabel(l); lbl.setForeground(TXT_DARK); lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            v = new JLabel(val); v.setForeground(c); v.setFont(new Font("Segoe UI", Font.BOLD, 58));
            add(lbl, BorderLayout.NORTH); add(v, BorderLayout.CENTER);
        }
        public void update(String val) { v.setText(val); }
    }

    private class AlertTile extends JPanel {
        public AlertTile(String n, int q) {
            setPreferredSize(new Dimension(260, 160)); setOpaque(false);
            setLayout(new BorderLayout()); setBorder(new EmptyBorder(30, 25, 30, 25));
            JLabel name = new JLabel("<html><b>" + n.toUpperCase() + "</b></html>"); name.setForeground(TXT_PRI); name.setFont(new Font("Segoe UI", Font.BOLD, 12));
            JLabel count = new JLabel(q + " UNITS"); count.setForeground(q==0 ? Color.RED : ACC_GOLD); count.setFont(new Font("Segoe UI", Font.BOLD, 32));
            JLabel msg = new JLabel(q==0? "EXHAUSTED" : "LOW POWER RESERVES"); msg.setForeground(TXT_DARK); msg.setFont(new Font("Segoe UI", Font.BOLD, 10));
            add(name, BorderLayout.NORTH); add(count, BorderLayout.CENTER); add(msg, BorderLayout.SOUTH);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(CLR_GLASS); g2.fillRoundRect(0,0,getWidth(),getHeight(),24,24);
            g2.setColor(BRD_COL); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,24,24); g2.dispose();
        }
    }

    private class GlassBox extends JPanel {
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(CLR_GLASS); g2.fillRoundRect(0,0,getWidth(),getHeight(),24,24);
            g2.setColor(BRD_COL); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,24,24); g2.dispose();
        }
        @Override public void setOpaque(boolean o) { super.setOpaque(false); }
    }

    private JTable buildTable(DefaultTableModel m, int[] w) {
        JTable t = new JTable(m) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        t.setBackground(new Color(0,0,0,0)); t.setForeground(TXT_PRI); t.setRowHeight(65);
        t.getTableHeader().setBackground(new Color(0,0,0,0)); t.getTableHeader().setForeground(TXT_DARK);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        t.setShowGrid(true); t.setGridColor(BRD_COL); 
        TableColumnModel cm = t.getColumnModel();
        for(int i=0; i<w.length && i<cm.getColumnCount(); i++) cm.getColumn(i).setPreferredWidth(w[i]);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
                if (!isSelected) setBackground(row % 2 == 0 ? new Color(20, 28, 48) : new Color(25, 33, 53));
                return c;
            }
        });
        return t;
    }

    private JPanel wrapTable(JTable t) { 
        JScrollPane s = new JScrollPane(t); 
        s.setOpaque(false); 
        s.getViewport().setOpaque(true); 
        s.getViewport().setBackground(new Color(15, 22, 38)); 
        s.setBorder(null); 
        
        JPanel g = new GlassBox(); 
        g.setLayout(new BorderLayout()); 
        g.add(s, BorderLayout.CENTER); 
        return g; 
    }

    private class TrendPlot extends GlassBox {
        private List<Double> d = new ArrayList<>();
        public void setData(List<Double> p) { d = p; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g); Graphics2D g2 = (Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(BRD_COL); g2.drawLine(85, 50, 85, getHeight()-80); g2.drawLine(85, getHeight()-80, getWidth()-50, getHeight()-80);
            g2.setColor(TXT_DARK); g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString("REVENUE VELOCITY ($)", 35, 35); g2.drawString("TIMELINE (MISSION IDS)", getWidth()-180, getHeight()-60);
            if(d.size()<2) { g2.dispose(); return; }
            double m = 0; for(double v : d) if(v>m) m = v;
            int w = getWidth()-180, h = getHeight()-180, step = w/(d.size()-1);
            Path2D.Double path = new Path2D.Double();
            for(int i=0; i<d.size(); i++){
                double x = 85 + i*step, y = (getHeight()-80) - (d.get(i)/m * h);
                if(i==0) path.moveTo(x,y); else path.lineTo(x,y);
                g2.setColor(ACC_GREEN); g2.fillOval((int)x-5, (int)y-5, 10, 10);
                if(d.get(i) == m || i % 2 == 0) { g2.setColor(TXT_PRI); g2.drawString("$" + String.format("%,.0f", d.get(i)), (int)x-18, (int)y-15); }
            }
            g2.setStroke(new BasicStroke(4f)); g2.draw(path); g2.dispose();
        }
    }

    private class StockDonut extends JPanel {
        private List<EnergyAsset> d = new ArrayList<>();
        public void setData(List<EnergyAsset> p) { d = p; repaint(); }
        public StockDonut() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            if(d.isEmpty()) return;
            Graphics2D g2 = (Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double s = 0; for(EnergyAsset a : d) s += a.getQuantity();
            double cur = 0; int x = getWidth()/2, y = getHeight()/2, r = 115;
            Color[] clrs = {ACC_CYAN, ACC_GREEN, ACC_GOLD, Color.MAGENTA, Color.ORANGE};
            for(int i=0; i<d.size(); i++){ double e = (d.get(i).getQuantity()/s)*360; g2.setColor(clrs[i%clrs.length]); g2.fill(new Arc2D.Double(x-r, y-r, 2*r, 2*r, cur, e, Arc2D.PIE)); cur += e; }
            g2.setColor(CLR_VOID); g2.fillOval(x-r/2, y-r/2, r, r);
            g2.setColor(TXT_PRI); g2.setFont(new Font("Segoe UI", Font.BOLD, 12)); g2.drawString("ASSET SHARE", x-40, y+5); g2.dispose();
        }
    }

    private JPanel topHead(String t, String s) {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
        JLabel tl = new JLabel(t); tl.setFont(new Font("Segoe UI", Font.BOLD, 42)); tl.setForeground(TXT_PRI);
        JLabel sl = new JLabel(s); sl.setFont(new Font("Segoe UI", Font.PLAIN, 16)); sl.setForeground(TXT_DARK);
        p.add(tl, BorderLayout.NORTH); p.add(sl, BorderLayout.SOUTH); return p;
    }

    private JButton styledBtn(String t, Color c, ActionListener a) {
        JButton b = new JButton(t) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c); g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16); g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 14)); b.setForeground(Color.WHITE); b.setFocusPainted(false); b.setBorderPainted(false); b.setContentAreaFilled(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR)); b.setPreferredSize(new Dimension(230, 58)); b.addActionListener(a); return b;
    }

    // CUSTOM GLASS DIALOG PORTAL
    private class GlassPortal extends JDialog {
        public GlassPortal(String title, JPanel content) {
            super(SolarisEnergyGUI.this, title, true);
            setUndecorated(true); setBackground(new Color(0,0,0,0));
            JPanel main = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(CLR_GLASS); g2.fillRoundRect(0,0,getWidth(),getHeight(),24,24);
                    g2.setColor(ACC_CYAN); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,24,24); g2.dispose();
                }
            };
            main.setOpaque(false);
            main.setBorder(new EmptyBorder(30, 30, 30, 30));
            
            JLabel head = new JLabel(title.toUpperCase()); head.setFont(new Font("Segoe UI", Font.BOLD, 18)); head.setForeground(ACC_CYAN); head.setBorder(new EmptyBorder(0,0,20,0));
            main.add(head, BorderLayout.NORTH); main.add(content, BorderLayout.CENTER);
            
            JPanel btnPnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0)); btnPnl.setOpaque(false); btnPnl.setBorder(new EmptyBorder(20,0,0,0));
            JButton ok = styledBtn("CONFIRM", ACC_GREEN, e -> dispose());
            JButton can = styledBtn("CANCEL", Color.GRAY, e -> { content.setName("CANCELLED"); dispose(); });
            btnPnl.add(can); btnPnl.add(ok); main.add(btnPnl, BorderLayout.SOUTH);
            
            setContentPane(main); pack(); setLocationRelativeTo(SolarisEnergyGUI.this);
        }
    }

    private void addInv() {
        JPanel p = new JPanel(new GridLayout(3, 2, 10, 15)); p.setOpaque(false);
        JTextField n = field(), pr = field(), q = field();
        p.add(lbl("Asset Name:")); p.add(n); p.add(lbl("Unit Price:")); p.add(pr); p.add(lbl("Quantity:")); p.add(q);
        GlassPortal gp = new GlassPortal("Hardware Registry", p); gp.setVisible(true);
        if(!"CANCELLED".equals(p.getName()) && !n.getText().isEmpty()) {
            backend.getInventory().add(n.getText(), Double.parseDouble(pr.getText()), Double.parseDouble(q.getText())); backend.save(); sync();
        }
    }

    private void addOrd() {
        JPanel p = new JPanel(new GridLayout(4, 2, 10, 15)); p.setOpaque(false);
        JComboBox<String> cb = combo(); 
        for(EnergyAsset a : backend.getInventory().getItems()) cb.addItem(a.getName());
        
        JTextField c = field(), l = field(), q = field();
        p.add(lbl("Select Asset:")); p.add(cb); p.add(lbl("Client Name:")); p.add(c); p.add(lbl("Mission Site:")); p.add(l); p.add(lbl("Unit Qty:")); p.add(q);
        GlassPortal gp = new GlassPortal("Mission Registry", p); gp.setVisible(true);
        if(!"CANCELLED".equals(p.getName()) && !c.getText().isEmpty()) {
            EnergyAsset a = backend.getInventory().find((String)cb.getSelectedItem());
            int requestedQty = Integer.parseInt(q.getText());
            if(a.getQuantity() >= requestedQty) {
                backend.getRequests().createOrder(backend.getInventory(), a.getName(), requestedQty, a.getPrice(), l.getText(), c.getText()); 
                backend.save(); sync();
            } else {
                JOptionPane.showMessageDialog(this, "INSUFFICIENT STOCK: Only " + (int)a.getQuantity() + " units available.", "GRID ERROR", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateInv() {
        JPanel p = new JPanel(new GridLayout(2, 2, 10, 15)); p.setOpaque(false);
        JComboBox<String> cb = combo();
        for(EnergyAsset a : backend.getInventory().getItems()) cb.addItem(a.getName());
        JTextField q = field();
        p.add(lbl("Select Asset:")); p.add(cb); p.add(lbl("Add Quantity:")); p.add(q);
        GlassPortal gp = new GlassPortal("Replenish Stock", p); gp.setVisible(true);
        if(!"CANCELLED".equals(p.getName()) && cb.getSelectedItem() != null) {
            try {
                backend.getInventory().addStock((String)cb.getSelectedItem(), Double.parseDouble(q.getText()));
                backend.save(); sync();
            } catch(Exception ex) {}
        }
    }

    private JComboBox<String> combo() {
        JComboBox<String> cb = new JComboBox<>();
        cb.setOpaque(false);
        cb.setBackground(new Color(255, 255, 255, 12));
        cb.setForeground(Color.WHITE);
        cb.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setBackground(isSelected ? ACC_CYAN : new Color(20, 28, 48));
                c.setForeground(isSelected ? CLR_VOID : Color.WHITE);
                c.setBorder(new EmptyBorder(8, 12, 8, 12));
                return c;
            }
        });
        cb.setBorder(new CompoundBorder(new LineBorder(BRD_COL, 1, true), new EmptyBorder(8, 10, 8, 10)));
        return cb;
    }

    private JTextField field() {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        f.setOpaque(false);
        f.setBackground(new Color(255, 255, 255, 12));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBorder(new CompoundBorder(new LineBorder(BRD_COL, 1, true), new EmptyBorder(10, 15, 10, 15)));
        return f;
    }
    private JLabel lbl(String t) { JLabel l = new JLabel(t); l.setForeground(TXT_DARK); l.setFont(new Font("Segoe UI", Font.BOLD, 12)); return l; }
    private void disp() { backend.processAll(); backend.save(); sync(); }

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new SolarisEnergyGUI().setVisible(true)); }
}
