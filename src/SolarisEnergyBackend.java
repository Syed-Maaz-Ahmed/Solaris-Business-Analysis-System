// Solaris Enterprise Backend: Deployment & Revenue Suites
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

class EnergyAsset {
    private String name;
    private double price; // Selling Price
    private double quantity;

    public EnergyAsset(String name, double price, double quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
}

class DeploymentOrder {
    private int orderId;
    private String assetName;
    private int quantity;
    private LocalDateTime date;
    private String status;
    private double totalRevenue;
    private String clientLocation;
    private String clientName;

    public DeploymentOrder(int id, String asset, int qty, LocalDateTime d, String s, double rev, String loc, String client) {
        this.orderId = id; this.assetName = asset; this.quantity = qty; this.date = d;
        this.status = s; this.totalRevenue = rev; this.clientLocation = loc; this.clientName = client;
    }

    public int getOrderId() { return orderId; }
    public String getAssetName() { return assetName; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getDate() { return date; }
    public String getStatus() { return status; }
    public double getTotalRevenue() { return totalRevenue; }
    public String getClientLocation() { return clientLocation; }
    public String getClientName() { return clientName; }
    public void setStatus(String s) { this.status = s; }
}

class InventoryManager {
    private ArrayList<EnergyAsset> items = new ArrayList<>();
    public ArrayList<EnergyAsset> getItems() { return items; }
    
    public void add(String n, double p, double q) { items.add(new EnergyAsset(n, p, q)); }
    
    public void addStock(String name, double qty) {
        EnergyAsset a = find(name);
        if (a != null) a.setQuantity(a.getQuantity() + qty);
    }
    
    public void load() {
        File f = new File("inventory.txt"); if(!f.exists()) return;
        try (Scanner sc = new Scanner(f)) {
            while(sc.hasNextLine()) {
                String[] d = sc.nextLine().split(",");
                if(d.length==3) items.add(new EnergyAsset(d[0], Double.parseDouble(d[1]), Double.parseDouble(d[2])));
            }
        } catch(Exception e) {}
    }
    
    public void save() {
        try (PrintWriter pw = new PrintWriter(new File("inventory.txt"))) {
            for(EnergyAsset a : items) pw.println(a.getName() + "," + a.getPrice() + "," + a.getQuantity());
        } catch(Exception e) {}
    }

    public EnergyAsset find(String name) {
        for(EnergyAsset a : items) if(a.getName().equalsIgnoreCase(name)) return a;
        return null;
    }

    public Stack<String> getAlerts() {
        Stack<String> alerts = new Stack<>();
        for(EnergyAsset a : items) {
            if(a.getQuantity() <= 5) alerts.push("CRITICAL: Low Stock on " + a.getName() + " (" + (int)a.getQuantity() + " units remaining)");
        }
        return alerts;
    }
}

class RevenueQueue {
    private Queue<DeploymentOrder> active = new LinkedList<>();
    private ArrayList<DeploymentOrder> history = new ArrayList<>();
    private double totalGlobalRevenue = 0;
    private int nextId = 1001;

    public RevenueQueue() { load(); }

    public void createOrder(InventoryManager inv, String asset, int qty, double price, String location, String client) {
        EnergyAsset a = inv.find(asset);
        if(a != null && a.getQuantity() >= qty) {
            a.setQuantity(a.getQuantity() - qty);
            active.add(new DeploymentOrder(nextId++, asset, qty, LocalDateTime.now(), "Pending", qty * price, location, client));
            inv.save();
        }
    }

    public DeploymentOrder process() {
        DeploymentOrder o = active.poll();
        if(o != null) {
            o.setStatus("Deployed");
            history.add(o);
            totalGlobalRevenue += o.getTotalRevenue();
            save();
        }
        return o;
    }

    public ArrayList<DeploymentOrder> getActive() { return new ArrayList<>(active); }
    public ArrayList<DeploymentOrder> getHistory() { return history; }
    public double getTotalRevenue() { return totalGlobalRevenue; }

    public void save() {
        try (PrintWriter pw = new PrintWriter(new File("orders_v2.txt"))) {
            pw.println(totalGlobalRevenue);
            for(DeploymentOrder o : active) pw.println(ser(o));
            for(DeploymentOrder o : history) pw.println(ser(o));
        } catch(Exception e) {}
    }

    private String ser(DeploymentOrder o) {
        return o.getOrderId() + "|" + o.getAssetName() + "|" + o.getQuantity() + "|" + (o.getDate() != null ? o.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)) + "|" + o.getStatus() + "|" + o.getTotalRevenue() + "|" + o.getClientLocation() + "|" + o.getClientName();
    }

    public void load() {
        File f = new File("orders_v2.txt"); if(!f.exists()) return;
        try (Scanner sc = new Scanner(f)) {
            if(sc.hasNextLine()) {
                String line = sc.nextLine();
                if(!line.isEmpty()) totalGlobalRevenue = Double.parseDouble(line);
            }
            while(sc.hasNextLine()) {
                String[] d = sc.nextLine().split("\\|");
                if(d.length >= 8) {
                    DeploymentOrder o = new DeploymentOrder(Integer.parseInt(d[0]), d[1], Integer.parseInt(d[2]), LocalDateTime.parse(d[3]), d[4], Double.parseDouble(d[5]), d[6], d[7]);
                    if(o.getStatus().equals("Pending")) active.add(o); else history.add(o);
                    if(o.getOrderId() >= nextId) nextId = o.getOrderId() + 1;
                }
            }
        } catch(Exception e) {}
    }
}

public class SolarisEnergyBackend {
    private InventoryManager inv = new InventoryManager();
    private RevenueQueue req = new RevenueQueue();

    public SolarisEnergyBackend() { inv.load(); }
    public InventoryManager getInventory() { return inv; }
    public RevenueQueue getRequests() { return req; }
    public void processAll() { while(req.process() != null); }
    public void save() { inv.save(); req.save(); }

    public static void main(String[] args) { SolarisEnergyGUI.main(args); }
}