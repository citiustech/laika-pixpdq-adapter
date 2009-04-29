package org.laika.pixpdqadapter;

import com.misyshealthcare.connect.net.Identifier;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.StringTokenizer;
import org.laika.pixpdqadapter.utils.MessageLogger;
import org.openhealthexchange.openpixpdq.data.Patient;
import org.openhealthexchange.openpixpdq.data.PatientIdentifier;
import org.openhealthexchange.openpixpdq.ihe.PixManagerException;

/**
 *
 * @author kananm
 */
public class PixPdqApi {

    // api to find patient id's
    public List<PatientIdentifier> findPatientIds(String idi, String id) {

        List patientList = null;
        PatientIdentifier pid = new PatientIdentifier(id, new Identifier(idi, "", ""));
        PixManagerAdapter pma = new PixManagerAdapter();

        try {
            patientList = pma.findPatientIds(pid, null);
        } catch (PixManagerException ex) {
            ex.printStackTrace();
        }

        return patientList;
    }

    // api to check if patient exists
    public String checkPatientExists(String idi, String id) {

        boolean patientFound = false;
        PatientIdentifier pid = new PatientIdentifier(id, new Identifier(idi, "", ""));
        PixManagerAdapter pma = new PixManagerAdapter();

        try {
            patientFound = pma.isValidPatient(pid, null);
        } catch (PixManagerException ex) {
            ex.printStackTrace();
        }

        if (patientFound) {
            return "Patient found";
        } else {
            return "Patient not found";
        }
    }

    public List<Patient> findPatient() {

        return null;
    }

    // api to convert List to string
    public String patientIdsToString(List<PatientIdentifier> patientList) {

        String retStr = "";
        String identifier = null;
        String domain = null;

        for (PatientIdentifier patientIdentifier : patientList) {
            identifier = patientIdentifier.getId();
            domain = patientIdentifier.getAssigningAuthority().getNamespaceId();
            retStr += "Patient Id: " + identifier + ",\t\t Identifier Domain Identifier: " + domain + "\n";
        }

        return retStr;
    }

    // main method
    public static void main(String[] args) {
        String menu = "Select: \n" +
                "1. PIX Feed\n" +
                "2. PIX Query\n" +
                "3. PD Query\n" +
                "4. Logs\n" +
                "Enter: ";
        System.out.print(menu);
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = null;
        try {
            st = new StringTokenizer(stdin.readLine(), ",");
        } catch (IOException ex) {
            //Logger.getLogger(PixPdqApi.class.getName()).log(Level.SEVERE, null, ex);
        }

        PixPdqApi api = new PixPdqApi();

        int sel = Integer.parseInt(st.nextToken());
        String idi=null;
        String id=null;

        try {
            idi = st.nextToken();
            id = st.nextToken();
        } catch (Exception e) {
        }

        switch (sel) {
            case 1:
                System.out.println(api.checkPatientExists(idi, id));
                break;
            case 2:
                System.out.println(api.patientIdsToString(api.findPatientIds(idi, id)));
                break;
            case 3:
                break;
            case 4:
                System.out.println (MessageLogger.getLog(5));
                break;
            default:
                System.exit(0);
        }
    }
}
