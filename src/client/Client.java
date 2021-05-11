package client;
//import static client.Client.scan;
import java.io.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.search.*;
public class Client {
    static class SMTPAuthenticator extends javax.mail.Authenticator {
        String _user, _password;
        public SMTPAuthenticator(String user, String pw){
            _user       = user;
            _password   = pw;
        }
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(_user, _password);
        }    
    }
    // coffeeK102@yahoo.com PW: anfxgtqdobprtzrv
    // sakurino84@yahoo.com PW: pgywouyccoerecdv
        static Scanner scan=new Scanner(System.in);
        // Globale Properties variable
        static Properties prop=new Properties();
        // Port-Nummern
        static String smtpPort      =   "587";
        static String popPort       =   "995";
        // Protokoll-Namen
        static String imapProtokoll =   "imaps";
        static String smtpProtokoll =   "smtp";
        static String popProtokoll  =   "pop3s";
        // Sever-Namen
        static String imapHost      =   "imap.mail.yahoo.com";
        static String smtpHost      =   "smtp.mail.yahoo.com";
        static String popHost       =   "pop.mail.yahoo.com";
        // Default-Nutzer- und passwort
        static String nutzer        =   "coffeeK102@yahoo.com ";
        static String passwort      =   "anfxgtqdobprtzrv";
        // Hilfsvariablen
        static String absender,empfänger,datum,betreff,inhalt,volltext,ordnerName;
        static int size=-1; // für die Suche nach der Zeilengröße
        static boolean moreInfo; // für das Anzeigen der Mail-Inhalte
       
    public static void main(String arg[]) throws Exception {
        System.out.println("Yahoo Mail-Client");
        Session sitzung;
        System.out.print("1) Anmelden    2) Default Konto verwenden ? : ");
        String s = scan.nextLine();
        while(!isInteger(s)  && s.length() > 1){
           System.out.print("1) Anmelden    2) Default Konto verwenden ? : "); 
           s = scan.nextLine();
        }
        if(s.charAt(0) == '1'){
            System.out.println("--- Anmeldung ---");
            sitzung = anmeldung();
            sitzung = createSitzung(nutzer,passwort);
        }
        else
            sitzung = createSitzung(nutzer,passwort);
        menue(sitzung);
    }
    // Angelehnt an die javax.sample - Dateien
    static Session anmeldung() throws MessagingException {
        System.out.print("\nEmailaddresse eingeben : ");
        nutzer = scan.nextLine();
        while(!isMail(nutzer)){
            System.out.print("Ungültige Addresse.\nEmailaddresse eingeben : ");
            nutzer = scan.nextLine();
        }
        System.out.printf("Passwort für %s : ",nutzer);
        passwort = scan.nextLine();
        Session x = createSitzung(nutzer,passwort); 
        System.out.println("Login . . .\n");  
        //Prüfe ob Email tatsächlich Funktionsfähig ist.
        try{
            Store isValid = x.getStore(imapProtokoll);
            isValid.connect(imapHost, nutzer, passwort);
        }
        catch(AuthenticationFailedException ax){
           System.out.printf("%s kann nicht eingeloggt werden. Bitte erneut versuchen.\n",nutzer);
           anmeldung(); 
        }
        catch(NoSuchProviderException nspe){
            System.out.printf("%s kann nicht eingeloggt werden. Bitte erneut versuchen.\n",nutzer);
            anmeldung(); 
        }
        System.out.printf("\nGrüßgott %s,\nWillkommen bei unserem kleinen Mailclient. Was möchtest du als nächstes tun?\n\n",nutzer);
        return x;
    }
    // Angelehnt an die javax.sample - Dateien
    static Session benutzerWechseln(Session sitzung) throws MessagingException {
        System.out.print("\nEmailaddresse eingeben : ");
        nutzer = scan.nextLine();
        while(!isMail(nutzer)){
            System.out.print("Ungültige Addresse.\nEmailaddresse eingeben : ");
            nutzer = scan.nextLine();
        }
        System.out.printf("Passwort für %s : ",nutzer);
        passwort = scan.nextLine();
        sitzung = createSitzung(nutzer,passwort); 
        System.out.println("Login . . .\n");  
        //Prüfe ob Email tatsächlich Funktionsfähig ist.
        try{
            Store isValid = sitzung.getStore(imapProtokoll);
            isValid.connect(imapHost, nutzer, passwort);
        }
        catch(AuthenticationFailedException ax){
           System.out.printf("%s kann nicht eingeloggt werden. Bitte erneut versuchen.\n",nutzer);
           anmeldung(); 
        }
        catch(NoSuchProviderException nspe){
            System.out.printf("%s kann nicht eingeloggt werden. Bitte erneut versuchen.\n",nutzer);
            anmeldung(); 
        }
        System.out.printf("\nGrüßgott %s,\nWillkommen bei unserem kleinen Mailclient. Was möchtest du als nächstes tun?\n\n",nutzer);
        return sitzung;
    }
    static void menue(Session sitzung){
        String s;
        while(true){
            System.out.print("\n1) Nachricht versenden    2) Ordner ansehen    3) Posteingang    4) Mail(s) suchen    5) Benutzer Wechseln    q) quit \n: ");
            s=scan.nextLine();
            if(s.charAt(0)=='q')
               return;
            while(!isInteger(s) || s.length()>1){
                System.out.print("Bitte nur eine natürliche Zahl eingeben : ");
                s = scan.nextLine();
            }
            try{
                switch(s.charAt(0)){
                    case '1': sendeMail (sitzung); break;
                    case '2': leseOrdner(sitzung); moreInfo = false; break;
                    case '3': leseMails (sitzung,"INBOX");  break;
                    case '4': sucheMails(sitzung); break;
                    case '5': sitzung = benutzerWechseln(sitzung); break;
                    case 'q': return;
                    default: break;
                }
            }
            catch(AddressException ae){
                System.out.println("Ungültige mail");
                //ae.printStackTrace();
            }
            catch(NoSuchProviderException nspe){
                System.out.println("Ungültiger Provider");
                //  nspe.printStackTrace();
            }
            catch(IOException ioex){
                System.out.println("Ungültige ");
                //ioex.printStackTrace();
            }
            catch(Exception x){
                System.out.println("Exception ......");
                //x.printStackTrace();
            }
        } 
    }
    // Angelehnt an die javax.sample - Datei
    static Session createSitzung(String _nutzer,String _passwort){
         //SMTP
        prop.put("mail.transport.protocol",smtpProtokoll);
        prop.put("mail.smtp.host",smtpHost);
        prop.put("mail.smtp.port",smtpPort);
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.auth","true");
        //POP
        prop.put("pop.mail.yahoo.com", popHost);
        prop.put("pop.mail.port", popPort);
        Authenticator auth = new SMTPAuthenticator(_nutzer,_passwort);
        Session sitzung = Session.getInstance(prop,auth);
       // session.setDebug(true);
       return sitzung;
    }
    // Angelehnt an die javax.sample - msgsend.java Datei
    static void sendeMail(Session sitzung) {
        nachrichtSchreiben(1);
        Message msg = new MimeMessage(sitzung);
        try{ // Setze Nachrichtendetails 
            msg.setFrom(new InternetAddress(nutzer));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(empfänger));
            msg.setSubject(betreff);
            msg.setText(inhalt);
            msg.setSentDate(new Date());
            System.out.println("\nSenden...");
            Transport.send(msg);
        }
        catch (MessagingException e) {
            throw new RuntimeException(e);
       }    
        System.out.println("\nMail wurde versendet.\n");
    }
    static void nachrichtSchreiben(int option) { 
        System.out.printf("Absender: %s\n",nutzer);
        System.out.print("Empfänger: ");
        System.out.flush();
        empfänger = scan.nextLine();
        // Eingabevalidierung
        while(!isMail(empfänger)){
            System.out.print("Ungültige Addresse.\nEmpfänger: ");
            empfänger = scan.nextLine();
        }
        System.out.print("Betreff: ");
        System.out.flush();
        betreff = scan.nextLine();
        System.out.print("Inhalt: \nZum versenden : #q (Enter) \n\n");
        System.out.flush();
        inhalt = setzeInhalt();
    }
    static String setzeInhalt(){
        StringBuilder sb=new StringBuilder();
        String s,t;
        int n=0;
        while(true){
            s = scan.nextLine();
            sb.append(s);
            if(sb.charAt(sb.length()-1) == 'q' && sb.charAt(sb.length()-2) == '#'){
                System.out.print("Abschicken?  y | n  : ");
                s = scan.nextLine();
                // Eingabevalidierung
                while(!isBuchstabe(s)){
                    System.out.print("Abschicken?  y | n  : ");
                    s = scan.nextLine();
                }
                if(s.charAt(0) == 'y'){
                    sb.delete(sb.length()-2, sb.length());
                    return sb.toString();
                }
                
                sb.delete(sb.length()-2, sb.length());
                System.out.println("Weiter im text: \n\n"+sb.toString());
            }  
            sb.append("\n");
        }
    }
    // Angelehnt an die javax.sample - folerlist.java Datei
    static void leseOrdner(Session sitzung) throws Exception{
        System.out.println("\nLaden...\n");
        try (Store store = sitzung.getStore(imapProtokoll)) {
            store.connect(imapHost, nutzer, passwort);
            System.out.println("STORE: "+store);
            //Um jeden Ordner anzusehen
            Folder []n = store.getDefaultFolder().list();
            String names[]=new String[n.length+1];
            int i=1;
            for(Folder ordner : n){
                System.out.println("* "+ordner.getName());
                names[i++]=ordner.getName();
                if(moreInfo){
                    if ((ordner.getType() & Folder.HOLDS_MESSAGES) != 0) {
                        System.out.println("Nachrichten Gesamt     : " + ordner.getMessageCount());
                        System.out.println("Neue Nachrichten       : " + ordner.getNewMessageCount());
                        System.out.println("Ungelesene Nachrichten : " + ordner.getUnreadMessageCount());
                        System.out.println("");
                    }              
                }
            }
            if(!moreInfo){
                System.out.print("\nMehr Ordner Details? y | n :");
                String s = scan.nextLine();
                while(!isBuchstabe(s) || s.length() > 1 ){
                    System.out.print("\nMehr Ordner Details? y | n :");
                    s = scan.nextLine();
                }
                if(s.charAt(0) == 'y'){
                    moreInfo=true;
                    leseOrdner(sitzung);
                    return;
                }
            }
           ordnerAuswahl(sitzung,names); 
        }
    }
    static void ordnerAuswahl(Session sitzung,String names[]) throws Exception{
            System.out.print("Ordner Auswählen?  y | n  : ");
            String auswahl=scan.nextLine();
            // Eingabevalidierung
            while(!isBuchstabe(auswahl) || auswahl.length() > 1){
                System.out.print("Ordner Auswählen?  y | n  : ");
                auswahl=scan.nextLine();
                System.out.println("");
            }
            if(auswahl.charAt(0)=='n')
                return;
            else{ // printed alle Ordner-Namen
                int i;
                for( i=1;i<names.length;i++){
                System.out.printf("%d) %s     ",i,names[i]);
                }
                System.out.print("\nOrdnerwahl: ");
                auswahl=scan.nextLine();
                // Eingabevalidierung
                while(!isInteger(auswahl) || auswahl.length()>1){
                    System.out.print("Ordnerwahl: ");
                    auswahl=scan.nextLine();
                }
            }
            ordnerName=names[Integer.parseInt(auswahl)];
            leseMails(sitzung,ordnerName);
    }
    // Angelehnt an Forembeiträge auf Tutorialspoint.com
    static void leseMails(Session sitzung,String ordnerName) throws Exception {
        System.out.println("\nLaden...\n");
        int i,n;
        String x;
        System.out.print("Wieviele nachrichten sollen gelesen werden? ");
        x = scan.nextLine();
        while(!isInteger(x)){
            System.out.print("Wieviele nachrichten sollen gelesen werden? ");
            x = scan.nextLine();  
        }
        n = Integer.parseInt(x);
        if(n < 0)
            n=0;
        if(moreInfo==false){
            System.out.print("Inhalte der Nachrichten anzeigen?  y | n : ");
            String q = scan.nextLine();
            // Eingabevalidierung
            while(!isBuchstabe(q) || q.length() > 1 ){
                System.out.print("Inhalte der Nachrichten anzeigen? anzeigen?  y || n : ");
                q = scan.nextLine();
            }
            if(q.charAt(0) == 'y'){
                    moreInfo = true; // zeigt nun mehr Ordner-Inhalte an
            }
        }
        try (Store korb = sitzung.getStore(imapProtokoll)) {
            korb.connect(imapHost,nutzer,passwort);
            Folder ordner = korb.getFolder(ordnerName);
            ordner.open(Folder.READ_ONLY);
            Message[] nachrichten = ordner.getMessages();
            System.out.printf("\n\n%s\n",ordnerName+" von\n"+nutzer+"\n"+nachrichten.length+" Nachrichten:\n");
            
            for (i = 0; i < nachrichten.length; i++) {
                Message nachricht = nachrichten[i];
                System.out.println("---------------------------------");
                System.out.printf("#%d. Nachricht\n", (i + 1));
                System.out.println("Von: "     + nachricht.getFrom()[0]);
                System.out.println("Am: "      + nachricht.getSentDate());
                System.out.println("Betreff: " + nachricht.getSubject());
               if(moreInfo)
                   System.out.println("Inhalt: \n"  + nachricht.getContent().toString());
               if(n <= i+1 )
                   break;
            }
            ordner.close(false);
        }
        catch(Exception ex){
            System.out.println("Error :");
            // ex.printStackTrace();
        }
        moreInfo = false;
    }// Angelehnt an die javax.sample - Search.java Datei
    static void sucheMails (Session sitzung) throws MessagingException {
      while(true){
        System.out.println("\n - - - Email(s) suchen - - -   Suchkriteriums-Auswahl");
        if(!sucheFragen())
            return;
        System.out.println("\nSuchen...");
        Store korb = sitzung.getStore(imapProtokoll);
        korb.connect(imapHost,nutzer,passwort);
       
        Folder ordner = korb.getDefaultFolder();
        
        Flags seen = new Flags(Flags.Flag.RECENT); 
        FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
        
        ordner = ordner.getFolder("INBOX");
        ordner.open(Folder.READ_ONLY);
        
        SearchTerm schlüssel = null;
        //Volltextsuche
        if(volltext != null){
           schlüssel =new BodyTerm(volltext);
        }
        //Suche nach Betreff
        if(betreff != null)
            schlüssel = new SubjectTerm(betreff);
        //Suche nach Absender
        if(absender != null) {
            FromStringTerm sucheAbs = new FromStringTerm(absender);
            if(schlüssel != null) {
                    schlüssel = new OrTerm(schlüssel,sucheAbs);
            }  
            else
                schlüssel = sucheAbs;
        }// Suche nach Datum
        if( datum != null){
            //Extrahiere das Datum aus String
            String [] tmp = datum.split("\\.");
            int tt = Integer.parseInt(tmp[0]);
            int mm = Integer.parseInt(tmp[1]);
            int jj = Integer.parseInt(tmp[2]);
            //Setze das Datum (Januar ist Monat 0)
            Calendar c = Calendar.getInstance();
            c.set(jj, mm-1, tt);
            //Setze das Suchintervall auf +- 1 Tag des angegebenen Datums
            ReceivedDateTerm morgen =  new ReceivedDateTerm(ComparisonTerm.GE, c.getTime());
            c.add(Calendar.DATE, 1);    // Tag +1
            ReceivedDateTerm abend =  new ReceivedDateTerm(ComparisonTerm.LT, c.getTime());
            //Setze Schlüssel auf
            SearchTerm tag = new AndTerm(morgen, abend);
            schlüssel = tag;
           
        } // Suche nach Zeilengröße
        if (size >= 0) {
        SearchTerm sizeTerm = new SizeTerm(ComparisonTerm.LE, size);
        schlüssel = sizeTerm;
        }
        // Suche
        Message [] msgs = ordner.search(schlüssel);
        if(msgs.length < 1)
              System.out.println("\nLeider Keine Übereinstimmung gefunden\n");
        else{
            
            System.out.printf("\n%d Übereinstimmungen gefunden\n",msgs.length);
       
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            ordner.fetch(msgs, fp);
            try{
                int i;
                for (i = 0; i < msgs.length; i++) {
                    System.out.println("--------------------------");
                    System.out.println("Nachricht #" + (i + 1) + ":");
                    gesuchteMails(msgs[i]);
                }
                
            }catch(Exception ex){
                System.out.println("Error " + ex.getMessage());
            }
        ordner.close(false);
        korb.close();
        }
      }
    }
    static boolean sucheFragen(){
        absender = empfänger = datum = betreff = null;
        System.out.print("1) Volltextsuche    2) Absender    3) Betreff    4) Datum    5) Zeilenanzahl     q) Suche beenden\n--> :");
        String x = scan.nextLine();
        if(x.charAt(0) == 'q')
            return false;
        while(!isInteger(x) || x.length() > 1 ){
            System.out.print("Bitte nur einen Buchstabe eingeben\n--> : ");
            x = scan.nextLine();
        }
        switch(x.charAt(0)){
            case '1' : System.out.println("Bitte geben Sie ein Schlüsselwort-, oder -satz ein: ");
                       volltext = scan.nextLine();
                       break;
            case '2' : System.out.print("(Such) Absender eingeben : ");
                       absender = scan.nextLine();
                       break;
            case '3' : System.out.print("(Such) Betreff eingeben : ");
                       betreff = scan.nextLine();
                       break;
            case '4' : System.out.print("(Such) Datum eingeben (tt.mm.jjjj) : ");
                       datum = scan.nextLine();
                       //Prüfe eingabe
                       while(!isDatum(datum)){
                           System.out.print("Datum bitte genau in diesem Format eingeben: (tt.mm.jjjj) : ");
                           datum = scan.nextLine();
                       }
                       break;
            case '5' : System.out.print("Maximal-Zeilenanzahl des Inhaltes eingeben : ");
                       String s = scan.nextLine();
                       //Prüfe eingabe
                       while(!isInteger(s) || Integer.parseInt(s) < 1){
                           System.out.print("Bitte nur natürliche Zahlen eingeben : ");
                           s = scan.nextLine();
                       }
                       size = Integer.parseInt(s);
                       break;
            default :  break;
        }
        return true;
    }
    // Angelehnt an die javax.sample - Dateien
    public static void gesuchteMails(Part p) throws Exception {
    if (p instanceof Message) {
        Message msg = (Message)p;
        Address[] a;
        if ((a = msg.getFrom()) != null) {
                int j;
        for ( j = 0; j < a.length; j++)
            System.out.println("Absender: " + a[j].toString());
        }
        if ((a = msg.getRecipients(Message.RecipientType.TO)) != null) {
                int j;
        for ( j = 0; j < a.length; j++)
            System.out.println("Empfänger: " + a[j].toString());
        }
        Date d = msg.getSentDate();
        System.out.println("Am: "           + d.toLocaleString());
        System.out.println("Betreff: : "    + msg.getSubject());
        System.out.println("Inhalt: \n"     + msg.getContent().toString());
        }
    }
    //Ab hier stehen die Fehlerbehandlungsfunktionen
    static boolean isInteger(String s){
        try {  
            Integer.parseInt(s);
            }  
            catch(NumberFormatException nfx){
                return false;  
            }catch(NullPointerException nx){
                return false;
            } return true;
    }
    // Inspiriert durch Forenbeiträge auf stackoverflow.com
    static boolean isBuchstabe(String s){
        return s.matches("[a-zA-Z]+");
    }
    static boolean isDatum(String s){
        if(s.length() < 10 || s.length() > 10)
            return false;
        //Prüfe auf Zahlen im Datumsformat
        StringBuilder x=new StringBuilder();
        x.append(s.charAt(0));
        x.append(s.charAt(1));
        if(!isInteger(x.toString())){
            return false;}
        x.append(s.charAt(3));
        x.append(s.charAt(4));
        if(!isInteger(x.toString())){
            return false;}
        x.append(s.charAt(6));
        x.append(s.charAt(7));
        x.append(s.charAt(8));
        x.append(s.charAt(9));
        if(!isInteger(x.toString())){
            return false;}
        x.delete(0, 10);
        System.out.println(""+x);
        x.append(s.charAt(2));
        x.append(s.charAt(5));
        System.out.println(""+x);
        // Prüfe auf Trenn-Punkt im Datumsformat
        for(int i=0;i<2;i++){
            if(x.charAt(i) != '.')
                return false;
        }
        // Nur wenn das Datum korrekt eingegeben wurde
        return true;
    }
    static boolean isMail(String s) {
        int a = 0, b = 0;
        //mindestens 5 Zeichen sollte die Mail schon lang sein :D
        if (s.length() < 5)
          return false;
        int i;
        for(i=0;i<s.length();i++){
            if(s.charAt(i) == '@')
                a = 1;// @ - Zeichen gefunden
        }
        for(i=0;i<s.length();i++){
            if(s.charAt(i) =='.')
                b = 1;
        }
        return (a+b == 2);       // falls '@' und '.' in der eingebenenen Emailaddr.
    }                            // auftauchen, return true --> gültige email
}