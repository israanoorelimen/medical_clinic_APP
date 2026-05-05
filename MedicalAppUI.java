import javax.swing.*; import javax.swing.table.*;
import java.awt.*; import java.awt.geom.*;
import java.sql.*; import java.text.SimpleDateFormat;
import java.util.Calendar; import java.util.Date;

public class MedicalAppUI extends JFrame {

    // ── Colors ────────────────────────────────────────────────
    static final Color BG=new Color(0xF0F4F8), CARD=new Color(0xFFFFFF),
        FLD=new Color(0xEDF2F7), BLUE=new Color(0x3182CE), GREEN=new Color(0x38A169),
        RED=new Color(0xE53E3E), ORG=new Color(0xDD6B20), PUR=new Color(0x805AD5),
        TXT=new Color(0x2D3748), DIM=new Color(0x718096),
        R1=new Color(0xFFFFFF), R2=new Color(0xF7FAFC),
        HDR=new Color(0xEBF8FF), BDR=new Color(0xBEE3F8);

    static final String[] HOURS={"09:00","09:30","10:00","10:30","11:00","11:30",
        "12:00","12:30","13:00","13:30","14:00","14:30","15:00","15:30","16:00","16:30","17:00","17:30"};
    static final String[] STATUS={"planifié","fait","annulé"};

    // ── Fields ────────────────────────────────────────────────
    private final String role;
    private DefaultTableModel pMdl, dMdl, rMdl;
    private JTable pTbl, dTbl, rTbl;
    private TableRowSorter<DefaultTableModel> pSrt, dSrt, rSrt;
    private JTextField tId,tNom,tPrn,tTel,tAdr,tYr,
                       dId,dNom,dPrn,dSpc,dTel,
                       rId,rPat,rDoc;
    private JSpinner rDt; private JComboBox<String> rHr,rSt;
    private JButton bDelP,bDelD,bDelR;

    // ── Constructor ───────────────────────────────────────────
    public MedicalAppUI(String role) {
        this.role=role;
        setTitle("Medical Clinic"); setSize(1100,720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); setLayout(new BorderLayout());
        getContentPane().setBackground(BG);
        add(topBar(),BorderLayout.NORTH);
        add(tabs(),  BorderLayout.CENTER);
        setVisible(true);
    }

    // ── Top bar ───────────────────────────────────────────────
    private JPanel topBar() {
        JPanel b=new JPanel(new BorderLayout()){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setPaint(new GradientPaint(0,0,new Color(0x2B6CB0),getWidth(),0,new Color(0x2C5282)));
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        b.setOpaque(false); b.setPreferredSize(new Dimension(0,60));
        b.setBorder(BorderFactory.createEmptyBorder(0,20,0,20));
        JLabel t=new JLabel("+ Medical Clinic"); t.setFont(new Font("Segoe UI",Font.BOLD,22)); t.setForeground(Color.WHITE);
        b.add(t,BorderLayout.WEST);
        // Role badge
        Color bc=role.equals("owner")?PUR:role.equals("doctor")?BLUE:ORG;
        String rt=role.equals("owner")?"OWNER":role.equals("doctor")?"DOCTOR":"ASSISTANT";
        JLabel badge=new JLabel("  "+rt+"  "); badge.setFont(new Font("Segoe UI",Font.BOLD,12));
        badge.setForeground(Color.WHITE); badge.setOpaque(false);
        // Logout
        JButton lo=btn("Logout",RED); lo.setPreferredSize(new Dimension(90,34));
        lo.addActionListener(e->{ if(ok("Logout?")){dispose(); new LoginPage();} });
        JPanel r=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,13)); r.setOpaque(false);
        r.add(badge); r.add(lo); b.add(r,BorderLayout.EAST);
        return b;
    }

    // ── Tabs ──────────────────────────────────────────────────
    private JTabbedPane tabs() {
        JTabbedPane t=new JTabbedPane();
        t.setFont(new Font("Segoe UI",Font.BOLD,13));
        t.setBackground(CARD); t.setForeground(TXT);
        t.addTab("  Patients  ",   patTab());
        t.addTab("  Doctors  ",    docTab());
        t.addTab("  Appointments  ",rdvTab());
        t.addChangeListener(e->{ for(int i=0;i<t.getTabCount();i++){
            t.setBackgroundAt(i,i==t.getSelectedIndex()?BLUE:CARD);
            t.setForegroundAt(i,Color.WHITE); }});
        t.setBackgroundAt(0,BLUE); t.setForegroundAt(0,Color.WHITE);
        return t;
    }

    // ── PATIENTS ──────────────────────────────────────────────
    private JPanel patTab() {
        pMdl=mdl("ID","Last Name","First Name","Phone","Address","Birth Year");
        pTbl=tbl(pMdl); pSrt=new TableRowSorter<>(pMdl); pTbl.setRowSorter(pSrt);
        tId=f("ID"); tNom=f("Last Name"); tPrn=f("First Name");
        tTel=f("Phone"); tAdr=f("Address"); tYr=f("Birth Year");
        JButton bA=btn("Add",GREEN),bU=btn("Update",BLUE); bDelP=btn("Delete",RED);
        bA.addActionListener(e->addP()); bU.addActionListener(e->updP()); bDelP.addActionListener(e->delP());
        role(role,bA,bU,bDelP);
        pTbl.getSelectionModel().addListSelectionListener(e->{
            int r=pTbl.getSelectedRow(); if(r<0)return; int m=pTbl.convertRowIndexToModel(r);
            tId.setText(s(pMdl,m,0)); tNom.setText(s(pMdl,m,1)); tPrn.setText(s(pMdl,m,2));
            tTel.setText(s(pMdl,m,3)); tAdr.setText(s(pMdl,m,4)); tYr.setText(s(pMdl,m,5));
        });
        loadP();
        return wrap(pTbl,pSrt,"Search patients...",
            row(2,6,l("ID"),l("LAST NAME"),l("FIRST NAME"),l("PHONE"),l("ADDRESS"),l("BIRTH YEAR"),
                    tId,tNom,tPrn,tTel,tAdr,tYr),
            bA,bU,bDelP);
    }
    private void loadP(){ load(pMdl,"SELECT num_patient,nom,prenom,telephone,adresse,EXTRACT(YEAR FROM date_naissance) FROM patient"); }
    private void addP(){
        String yr=tYr.getText().trim();
        if(!yr.isEmpty()){ try{ int y=Integer.parseInt(yr); if(y<1900||y>2024){warn("Year must be 1900–2024");return;}}catch(NumberFormatException e){warn("Year must be a number");return;} }
        run("INSERT INTO patient(num_patient,nom,prenom,telephone,adresse,date_naissance) VALUES((SELECT NVL(MAX(num_patient),0)+1 FROM patient),?,?,?,?,TO_DATE(?,'YYYY-MM-DD'))",
            tNom.getText(),tPrn.getText(),tTel.getText(),tAdr.getText(),yr.isEmpty()?null:yr+"-01-01");
        loadP(); clrP();
    }
    private void updP(){
        run("UPDATE patient SET nom=?,prenom=?,telephone=?,adresse=? WHERE num_patient=?",
            tNom.getText(),tPrn.getText(),tTel.getText(),tAdr.getText(),tId.getText());
        loadP(); clrP();
    }
    private void delP(){ if(ok("Delete patient?")) run("DELETE FROM patient WHERE num_patient=?",tId.getText()); loadP(); clrP(); }
    private void clrP(){ for(JTextField f:new JTextField[]{tId,tNom,tPrn,tTel,tAdr,tYr}) f.setText(""); }

    // ── DOCTORS ───────────────────────────────────────────────
    private JPanel docTab() {
        dMdl=mdl("ID","Last Name","First Name","Specialty","Phone");
        dTbl=tbl(dMdl); dSrt=new TableRowSorter<>(dMdl); dTbl.setRowSorter(dSrt);
        dId=f("ID"); dNom=f("Last Name"); dPrn=f("First Name"); dSpc=f("Specialty"); dTel=f("Phone");
        JButton bA=btn("Add",GREEN),bU=btn("Update",BLUE); bDelD=btn("Delete",RED);
        bA.addActionListener(e->addD()); bU.addActionListener(e->updD()); bDelD.addActionListener(e->delD());
        if(!role.equals("owner")){ bA.setEnabled(false); bU.setEnabled(false); bDelD.setEnabled(false); }
        dTbl.getSelectionModel().addListSelectionListener(e->{
            int r=dTbl.getSelectedRow(); if(r<0)return; int m=dTbl.convertRowIndexToModel(r);
            dId.setText(s(dMdl,m,0)); dNom.setText(s(dMdl,m,1)); dPrn.setText(s(dMdl,m,2));
            dSpc.setText(s(dMdl,m,3)); dTel.setText(s(dMdl,m,4));
        });
        loadD();
        return wrap(dTbl,dSrt,"Search doctors...",
            row(2,5,l("ID"),l("LAST NAME"),l("FIRST NAME"),l("SPECIALTY"),l("PHONE"),
                    dId,dNom,dPrn,dSpc,dTel),
            bA,bU,bDelD);
    }
    private void loadD(){ load(dMdl,"SELECT num_medecin,nom,prenom,specialite,telephone FROM medecin"); }
    private void addD(){
        run("INSERT INTO medecin(num_medecin,nom,prenom,specialite,telephone) VALUES((SELECT NVL(MAX(num_medecin),0)+1 FROM medecin),?,?,?,?)",
            dNom.getText(),dPrn.getText(),dSpc.getText(),dTel.getText());
        loadD(); clrD();
    }
    private void updD(){
        run("UPDATE medecin SET nom=?,prenom=?,specialite=?,telephone=? WHERE num_medecin=?",
            dNom.getText(),dPrn.getText(),dSpc.getText(),dTel.getText(),dId.getText());
        loadD(); clrD();
    }
    private void delD(){ if(ok("Delete doctor?")) run("DELETE FROM medecin WHERE num_medecin=?",dId.getText()); loadD(); clrD(); }
    private void clrD(){ for(JTextField f:new JTextField[]{dId,dNom,dPrn,dSpc,dTel}) f.setText(""); }

    // ── APPOINTMENTS ──────────────────────────────────────────
    private JPanel rdvTab() {
        rMdl=mdl("ID","Patient","Doctor","Date","Time","Status");
        rTbl=tbl(rMdl); rSrt=new TableRowSorter<>(rMdl); rTbl.setRowSorter(rSrt);
        // Color status column
        rTbl.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int row,int col){
                super.getTableCellRendererComponent(t,v,sel,foc,row,col);
                if(!sel){ setBackground(row%2==0?R1:R2);
                    String sv=v!=null?v.toString():"";
                    setForeground(sv.equals("planifié")?BLUE:sv.equals("annulé")?RED:GREEN); }
                setFont(new Font("Segoe UI",Font.BOLD,13));
                setBorder(BorderFactory.createEmptyBorder(0,12,0,12));
                return this;
            }
        });
        // Search by patient + doctor name
        JPanel sr=new JPanel(new GridLayout(1,4,8,0)); sr.setBackground(BG);
        sr.setBorder(BorderFactory.createEmptyBorder(0,0,8,0));
        JTextField sp=f("Patient name..."), sd=f("Doctor name...");
        sr.add(l("PATIENT")); sr.add(sp); sr.add(l("DOCTOR")); sr.add(sd);
        Runnable flt=()->{
            String p=sp.getText().trim(), d=sd.getText().trim();
            if(p.isEmpty()&&d.isEmpty()){rSrt.setRowFilter(null);return;}
            java.util.List<RowFilter<Object,Object>> fs=new java.util.ArrayList<>();
            if(!p.isEmpty()) fs.add(RowFilter.regexFilter("(?i)"+p,1));
            if(!d.isEmpty()) fs.add(RowFilter.regexFilter("(?i)"+d,2));
            rSrt.setRowFilter(RowFilter.andFilter(fs));
        };
        listen(sp,flt); listen(sd,flt);
        // Date spinner
        Calendar mn=Calendar.getInstance(); mn.set(2024,0,1);
        Calendar mx=Calendar.getInstance(); mx.set(2030,11,31);
        rDt=new JSpinner(new SpinnerDateModel(new Date(),mn.getTime(),mx.getTime(),Calendar.DAY_OF_MONTH));
        rDt.setEditor(new JSpinner.DateEditor(rDt,"yyyy-MM-dd")); styleSpinner(rDt);
        rHr=cbo(HOURS); rSt=cbo(STATUS);
        rId=f("ID"); rPat=f("Patient ID"); rDoc=f("Doctor ID");
        JButton bA=btn("Add",GREEN),bU=btn("Update",BLUE); bDelR=btn("Delete",RED);
        bA.addActionListener(e->addR()); bU.addActionListener(e->updR()); bDelR.addActionListener(e->delR());
        role(role,bA,bU,bDelR);
        rTbl.getSelectionModel().addListSelectionListener(e->{
            int r=rTbl.getSelectedRow(); if(r<0)return; int m=rTbl.convertRowIndexToModel(r);
            rId.setText(s(rMdl,m,0));
            try{ rDt.setValue(new SimpleDateFormat("yyyy-MM-dd").parse(s(rMdl,m,3))); }catch(Exception ex){}
            rHr.setSelectedItem(s(rMdl,m,4)); rSt.setSelectedItem(s(rMdl,m,5));
        });
        loadR();
        // Build panel manually (custom search row)
        JScrollPane sc=new JScrollPane(rTbl); sc.getViewport().setBackground(CARD);
        sc.setBorder(BorderFactory.createLineBorder(BDR,1));
        JPanel top=new JPanel(new BorderLayout(0,8)); top.setBackground(BG);
        top.setBorder(BorderFactory.createEmptyBorder(12,12,0,12));
        top.add(sr,BorderLayout.NORTH); top.add(sc,BorderLayout.CENTER);
        JPanel form=row(2,7,l("ID"),l("PATIENT ID"),l("DOCTOR ID"),l("DATE"),l("TIME"),l("STATUS"),l(""),
                        rId,rPat,rDoc,rDt,rHr,rSt,new JLabel(""));
        JPanel south=new JPanel(new BorderLayout(12,0)); south.setBackground(CARD);
        south.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0,BDR),
            BorderFactory.createEmptyBorder(12,16,12,16)));
        south.add(form,BorderLayout.CENTER); south.add(btns(bA,bU,bDelR),BorderLayout.EAST);
        JPanel p=new JPanel(new BorderLayout()); p.setBackground(BG);
        p.add(top,BorderLayout.CENTER); p.add(south,BorderLayout.SOUTH);
        return p;
    }
    private void loadR(){
        load(rMdl,"SELECT r.num_rendezvous,p.nom||' '||p.prenom,m.nom||' '||m.prenom,"
            +"TO_CHAR(r.date_rdv,'YYYY-MM-DD'),r.heure_rdv,r.statut "
            +"FROM rendezvous r JOIN patient p ON r.num_patient=p.num_patient "
            +"JOIN medecin m ON r.num_medecin=m.num_medecin ORDER BY r.date_rdv,r.heure_rdv");
    }
    private void addR(){
        Calendar c=Calendar.getInstance(); c.setTime((Date)rDt.getValue());
        int day=c.get(Calendar.DAY_OF_WEEK);
        if(day==Calendar.FRIDAY||day==Calendar.SATURDAY){warn("Sunday–Thursday only.");return;}
        String dt=new SimpleDateFormat("yyyy-MM-dd").format((Date)rDt.getValue());
        String hr=(String)rHr.getSelectedItem(), st=(String)rSt.getSelectedItem();
        if(conflict(rDoc.getText(),dt,hr)){warn("Doctor busy at "+hr+" on "+dt+". Choose another slot.");return;}
        run("INSERT INTO rendezvous(num_rendezvous,num_patient,num_medecin,date_rdv,heure_rdv,statut) "
           +"VALUES((SELECT NVL(MAX(num_rendezvous),0)+1 FROM rendezvous),?,?,TO_DATE(?,'YYYY-MM-DD'),?,?)",
            rPat.getText(),rDoc.getText(),dt,hr,st);
        loadR(); clrR();
    }
    private void updR(){
        String dt=new SimpleDateFormat("yyyy-MM-dd").format((Date)rDt.getValue());
        run("UPDATE rendezvous SET date_rdv=TO_DATE(?,'YYYY-MM-DD'),heure_rdv=?,statut=? WHERE num_rendezvous=?",
            dt,rHr.getSelectedItem(),rSt.getSelectedItem(),rId.getText());
        loadR(); clrR();
    }
    private void delR(){ if(ok("Delete appointment?")) run("DELETE FROM rendezvous WHERE num_rendezvous=?",rId.getText()); loadR(); clrR(); }
    private void clrR(){ rId.setText(""); rPat.setText(""); rDoc.setText(""); rDt.setValue(new Date()); rHr.setSelectedIndex(0); rSt.setSelectedIndex(0); }
    private boolean conflict(String docId,String dt,String hr){
        try(Connection c=Connexion.getConnexion();
            PreparedStatement ps=c.prepareStatement("SELECT COUNT(*) FROM rendezvous WHERE num_medecin=? AND date_rdv=TO_DATE(?,'YYYY-MM-DD') AND heure_rdv=? AND statut!='annulé'")){
            ps.setInt(1,Integer.parseInt(docId)); ps.setString(2,dt); ps.setString(3,hr);
            ResultSet rs=ps.executeQuery(); return rs.next()&&rs.getInt(1)>0;
        }catch(Exception e){return false;}
    }

    // ─────────────────────────────────────────────────────────
    // SHARED HELPERS
    // ─────────────────────────────────────────────────────────

    /** Generic tab: search + table + form + buttons */
    private JPanel wrap(JTable table, TableRowSorter<?> srt, String hint, JPanel form, JButton bA, JButton bU, JButton bD){
        JScrollPane sc=new JScrollPane(table); sc.getViewport().setBackground(CARD);
        sc.setBorder(BorderFactory.createLineBorder(BDR,1));
        JPanel top=new JPanel(new BorderLayout(0,8)); top.setBackground(BG);
        top.setBorder(BorderFactory.createEmptyBorder(12,12,0,12));
        top.add(search(srt,hint),BorderLayout.NORTH); top.add(sc,BorderLayout.CENTER);
        JPanel south=new JPanel(new BorderLayout(12,0)); south.setBackground(CARD);
        south.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0,BDR),
            BorderFactory.createEmptyBorder(12,16,12,16)));
        south.add(form,BorderLayout.CENTER); south.add(btns(bA,bU,bD),BorderLayout.EAST);
        JPanel p=new JPanel(new BorderLayout()); p.setBackground(BG);
        p.add(top,BorderLayout.CENTER); p.add(south,BorderLayout.SOUTH);
        return p;
    }

    private DefaultTableModel mdl(String... cols){
        return new DefaultTableModel(cols,0){ @Override public boolean isCellEditable(int r,int c){return false;} };
    }

    private JTable tbl(DefaultTableModel m){
        JTable t=new JTable(m);
        t.setBackground(R1); t.setForeground(TXT); t.setGridColor(BDR); t.setRowHeight(34);
        t.setFont(new Font("Segoe UI",Font.PLAIN,13));
        t.setSelectionBackground(new Color(0xBEE3F8)); t.setSelectionForeground(TXT);
        t.setShowVerticalLines(false); t.setIntercellSpacing(new Dimension(0,1));
        t.setDefaultRenderer(Object.class,new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable tb,Object v,boolean sel,boolean foc,int row,int col){
                super.getTableCellRendererComponent(tb,v,sel,foc,row,col);
                if(!sel){setBackground(row%2==0?R1:R2);setForeground(TXT);}
                setBorder(BorderFactory.createEmptyBorder(0,12,0,12));
                return this;
            }
        });
        JTableHeader h=t.getTableHeader(); h.setBackground(HDR); h.setForeground(BLUE);
        h.setFont(new Font("Segoe UI",Font.BOLD,13));
        h.setBorder(BorderFactory.createMatteBorder(0,0,2,0,BLUE));
        h.setReorderingAllowed(false);
        ((DefaultTableCellRenderer)h.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        return t;
    }

    private JTextField f(String tip){
        JTextField f=new JTextField(); f.setToolTipText(tip);
        f.setBackground(FLD); f.setForeground(TXT); f.setCaretColor(TXT);
        f.setFont(new Font("Segoe UI",Font.PLAIN,13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BDR,1),BorderFactory.createEmptyBorder(6,10,6,10)));
        return f;
    }

    private JLabel l(String t){ JLabel l=new JLabel(t); l.setForeground(DIM); l.setFont(new Font("Segoe UI",Font.BOLD,11)); return l; }

    private JButton btn(String text,Color c){
        JButton b=new JButton(text){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled()?(getModel().isRollover()?c.brighter():c):new Color(0xCBD5E0));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(isEnabled()?Color.WHITE:DIM);
                g2.setFont(new Font("Segoe UI",Font.BOLD,13));
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
        };
        b.setPreferredSize(new Dimension(120,38));
        b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.setFocusPainted(false);
        return b;
    }

    private JComboBox<String> cbo(String[] items){
        JComboBox<String> c=new JComboBox<>(items);
        c.setBackground(FLD); c.setForeground(TXT); c.setFont(new Font("Segoe UI",Font.PLAIN,13));
        return c;
    }

    private void styleSpinner(JSpinner s){
        JSpinner.DefaultEditor e=(JSpinner.DefaultEditor)s.getEditor();
        e.getTextField().setBackground(FLD); e.getTextField().setForeground(TXT);
        e.getTextField().setCaretColor(TXT);
        e.getTextField().setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
    }

    private JPanel row(int rows,int cols,Component... cs){
        JPanel p=new JPanel(new GridLayout(rows,cols,8,6)); p.setBackground(CARD);
        for(Component c:cs) p.add(c); return p;
    }

    private JPanel btns(JButton bA,JButton bU,JButton bD){
        JPanel p=new JPanel(new GridLayout(3,1,0,8)); p.setBackground(CARD);
        p.setBorder(BorderFactory.createEmptyBorder(0,8,0,0));
        p.add(bA); p.add(bU); p.add(bD); return p;
    }

    private JPanel search(TableRowSorter<?> srt,String hint){
        JPanel p=new JPanel(new BorderLayout(8,0)); p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        JLabel ic=new JLabel("  Search:"); ic.setFont(new Font("Segoe UI",Font.BOLD,13)); ic.setForeground(BLUE);
        JTextField sf=f(hint); sf.setText(hint); sf.setForeground(DIM);
        sf.addFocusListener(new java.awt.event.FocusAdapter(){
            public void focusGained(java.awt.event.FocusEvent e){if(sf.getText().equals(hint)){sf.setText("");sf.setForeground(TXT);}}
            public void focusLost(java.awt.event.FocusEvent e){if(sf.getText().isEmpty()){sf.setText(hint);sf.setForeground(DIM);}}
        });
        listen(sf,()->{ String t=sf.getText().trim(); srt.setRowFilter(t.isEmpty()||t.equals(hint)?null:RowFilter.regexFilter("(?i)"+t)); });
        JButton clr=btn("Clear",new Color(0xA0AEC0)); clr.setPreferredSize(new Dimension(70,36));
        clr.addActionListener(e->{sf.setText(hint);sf.setForeground(DIM);srt.setRowFilter(null);});
        p.add(ic,BorderLayout.WEST); p.add(sf,BorderLayout.CENTER); p.add(clr,BorderLayout.EAST);
        return p;
    }

    private void listen(JTextField f,Runnable r){
        f.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            public void insertUpdate(javax.swing.event.DocumentEvent e){r.run();}
            public void removeUpdate(javax.swing.event.DocumentEvent e){r.run();}
            public void changedUpdate(javax.swing.event.DocumentEvent e){r.run();}
        });
    }

    private void load(DefaultTableModel m,String sql){
        m.setRowCount(0);
        try(Connection c=Connexion.getConnexion();Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){
            int cols=r.getMetaData().getColumnCount();
            while(r.next()){ Object[] row=new Object[cols]; for(int i=0;i<cols;i++) row[i]=r.getObject(i+1); m.addRow(row); }
        }catch(Exception e){err("Load error: "+e.getMessage());}
    }

    private void run(String sql,Object... params){
        try(Connection c=Connexion.getConnexion();PreparedStatement ps=c.prepareStatement(sql)){
            for(int i=0;i<params.length;i++)
                if(params[i]==null) ps.setNull(i+1,Types.VARCHAR); else ps.setString(i+1,params[i].toString());
            ps.executeUpdate();
        }catch(SQLException e){err("Error: "+e.getMessage());}
    }

    private void role(String role,JButton a,JButton u,JButton d){
        if(role.equals("doctor")){a.setEnabled(false);u.setEnabled(false);d.setEnabled(false);}
        if(role.equals("assistant")){d.setEnabled(false);}
    }

    private String s(DefaultTableModel m,int r,int c){ Object v=m.getValueAt(r,c); return v!=null?v.toString():""; }
    private void warn(String msg){JOptionPane.showMessageDialog(this,msg,"Warning",JOptionPane.WARNING_MESSAGE);}
    private void err(String msg){JOptionPane.showMessageDialog(this,msg,"Error",JOptionPane.ERROR_MESSAGE);}
    private boolean ok(String msg){return JOptionPane.showConfirmDialog(this,msg,"Confirm",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION;}

    public static void main(String[] args){ SwingUtilities.invokeLater(()->new MedicalAppUI("owner")); }
}