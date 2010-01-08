package org.laika.pixpdqadapter;

import com.misyshealthcare.connect.net.Identifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.laika.pixpdqadapter.utils.DbUtils;
import org.openhealthexchange.openpixpdq.data.MessageHeader;
import org.openhealthexchange.openpixpdq.data.Patient;
import org.openhealthexchange.openpixpdq.data.PatientIdentifier;
import org.openhealthexchange.openpixpdq.ihe.IPixManagerAdapter;
import org.openhealthexchange.openpixpdq.ihe.PixManagerException;

public class PixManagerAdapter implements IPixManagerAdapter {

    private static final Logger log = Logger.getLogger(PixManagerAdapter.class.getName());

    public PixManagerAdapter() {
    }

    /**
     * Accepts the patient id and identifier domain identifier,
     * checks if the patient exists in the database and returns either true or false.
     *
     * @param pid contains the patient identification details
     * @param header
     * @return if the find was successful
     * @throws org.openhealthexchange.openpixpdq.ihe.PixManagerException
     */
    public boolean isValidPatient(PatientIdentifier pid, MessageHeader header) throws PixManagerException {

//        boolean flag = false;
//
//        String sql = "SELECT count(*) patient_id " +
//                "FROM patient_identifiers " +
//                "where affinity_domain in ('" +
//                pid.getAssigningAuthority().getAuthorityNameString() +
//                "', '" + pid.getAssigningAuthority().getNamespaceId() +
//                "', '&" + pid.getAssigningAuthority().getUniversalId() + "&" +
//                pid.getAssigningAuthority().getUniversalIdType() +
//                "') and patient_identifier = '" + pid.getId() + "'";
//
//        log.log(Level.INFO, "SQL to select patient id for patient: " + sql);
//
//        int patientID = getPatientId(sql);
//
//        if (patientID != 0) {
//            flag = true;
//        }
//
//        return flag;

        return true;
    }

    /**
     * Accepts the patient id and identifier domain identifier and
     * returns a list of patient identifiers that exists for the corresponding patient.
     *
     * @param pid contains the patient identification details
     * @param header
     * @return a list of Patinet Ids
     * @throws org.openhealthexchange.openpixpdq.ihe.PixManagerException
     */
    public List<PatientIdentifier> findPatientIds(PatientIdentifier pid, MessageHeader header)
            throws PixManagerException {

        List patientList = new ArrayList();

        String sql = "SELECT distinct patient_id " + "from patient_identifiers pi, test_plans vtp, patients pd "
			+ "where pd.id = pi.patient_id and pd.test_plan_id = vtp.id and vtp.type = 'PixQueryPlan' "
			+ "and affinity_domain in ('" + pid.getAssigningAuthority().getAuthorityNameString() + "', '"
			+ pid.getAssigningAuthority().getNamespaceId() + "', '&" + pid.getAssigningAuthority().getUniversalId()
			+ "&" + pid.getAssigningAuthority().getUniversalIdType() + "') and patient_identifier = '"
			+ pid.getId() + "'";

        log.log(Level.INFO, "SQL to select patient id for patient: " + sql);

        int patientID = getPatientId(sql);

        if (patientID != 0) {

            ResultSet rsPatient = null;

            try {
                // select all patient identifiers and idi for the patient
                sql = "SELECT patient_identifier, affinity_domain " + "FROM patient_identifiers "
					+ "where patient_id = " + patientID;

                log.log(Level.INFO, "SQL to select id and idi for found patient: " + sql);

                DbUtils.getInstance().openConnection();
                rsPatient = DbUtils.getInstance().executeQuery(sql);

                while (rsPatient.next()) {
                    log.log(Level.INFO, "Patient ID : " + rsPatient.getString("patient_identifier") + "; "
						+ "Patient IDI: " + rsPatient.getString("affinity_domain"));

                    String[] identHD = rsPatient.getString("affinity_domain").split("&");

                    Identifier identifier = null;

                    if (identHD.length == 3) {
                        if (identHD[0].length() == 0) {
                            identifier = new Identifier(null, identHD[1], identHD[2]);
                        } else {
                            identifier = new Identifier(identHD[0], identHD[1], identHD[2]);
                        }
                    } else {
                        identifier = new Identifier(identHD[0], "", "");
                    }

                    patientList.add(new PatientIdentifier(rsPatient.getString("patient_identifier"),
                            identifier));
                }
            } catch (SQLException ex) {
                log.error("SQLException: " + ex.getMessage());
            } finally {
                DbUtils.getInstance().closeConnection();
            }
        }

        return patientList;
    }

    /**
     *
     * @param patient details of the patient
     * @param header
     * @throws org.openhealthexchange.openpixpdq.ihe.PixManagerException
     */
    public List<List<PatientIdentifier>> updatePatient(Patient patient, MessageHeader header) throws PixManagerException {
        insertPatientId(patient);

        return null;
    }

    public List<List<PatientIdentifier>> mergePatients(Patient patientMain, Patient patientOld, MessageHeader header)
            throws PixManagerException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     *
     * @param patient details of the patient
     * @param header
     * @throws org.openhealthexchange.openpixpdq.ihe.PixManagerException
     */
    public List<PatientIdentifier> createPatient(Patient patient, MessageHeader header) throws PixManagerException {
        insertPatientId(patient);

        return null;
    }

    // Adds a new patient identifier to the database.
    private void insertPatientId(Patient patient) {

        String first_name = patient.getPatientName().getFirstName();
        String last_name = patient.getPatientName().getLastName();
        String sql;

        //select patient data id
        sql = "select pd.id patient_id from patients pd , test_plans vp "
			+ "where pd.test_plan_id = vp.id and vp.type = 'PixFeedPlan' "
			+ "and lower(pd.name) = lower('" + first_name + "') +' '+ lower('" + last_name
			+ "') order by pd.updated_at desc";

        log.log(Level.INFO, "SQL to select PIX patient id for patient: " + sql);

        int patientID = getPatientId(sql);

        if (patientID != 0) {

            String pat_id = ((PatientIdentifier) patient.getPatientIds().get(0)).getId();
            String idi = ((Identifier) ((PatientIdentifier) patient.getPatientIds().get(0)).getAssigningAuthority()).getAuthorityNameString();

            ResultSet rsPatient = null;

            //check if id exists in db
            sql = "select count(*) cnt FROM patient_identifiers " + "where patient_id = "
				+ patientID + " and affinity_domain ='" + idi + "' " + "and patient_identifier ='"
				+ pat_id + "'";

            log.log(Level.INFO, "SQL to check for existing ID and IDI: " + sql);

            try {
                DbUtils.getInstance().openConnection();
                rsPatient = DbUtils.getInstance().executeQuery(sql);

                if (rsPatient.next()) {
                    log.log(Level.INFO, "Matching ids found: " + rsPatient.getInt("cnt"));
                    if (rsPatient.getInt("cnt") == 0) {

                        //insert identifier into db
                        sql = "insert into patient_identifiers(patient_id, affinity_domain, "
							+ "patient_identifier) " + "values(" + patientID + ", '" + idi + "', '"
							+ pat_id + "')";

                        log.log(Level.INFO, "SQL to insert patient id and idi: " + sql);

                        DbUtils.getInstance().executeUpdate(sql);

                        log.log(Level.INFO, "Patient info added sucessfully.");
                    }
                }
            } catch (SQLException ex) {
                log.error("SQLException: " + ex.getMessage());
            } finally {
                DbUtils.getInstance().closeConnection();
            }
        }
    }

    // get patient id
    private int getPatientId(String sql) {

        ResultSet rsPatient = null;
        int patientId = 0;

        DbUtils.getInstance().openConnection();
        rsPatient =
                DbUtils.getInstance().executeQuery(sql);
        try {
            while (rsPatient.next()) {
                patientId = rsPatient.getInt("patient_id");
                log.log(Level.INFO, "Found PatientId: " + patientId);
                break;

            }
        } catch (SQLException ex) {
            log.error("SQLException: " + ex.getMessage());
        } finally {
            DbUtils.getInstance().closeConnection();
        }

        return patientId;
    }
}
