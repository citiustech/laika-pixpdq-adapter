package org.laika.pixpdqadapter;

import com.misyshealthcare.connect.base.SharedEnums.PhoneType;
import com.misyshealthcare.connect.base.SharedEnums.SexType;
import com.misyshealthcare.connect.base.demographicdata.Address;
import com.misyshealthcare.connect.base.demographicdata.PhoneNumber;
import com.misyshealthcare.connect.net.Identifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.laika.pixpdqadapter.utils.DbUtils;
import org.openhealthexchange.openpixpdq.data.MessageHeader;
import org.openhealthexchange.openpixpdq.data.Patient;
import org.openhealthexchange.openpixpdq.data.PatientIdentifier;
import org.openhealthexchange.openpixpdq.data.PersonName;
import org.openhealthexchange.openpixpdq.ihe.IPdSupplierAdapter;
import org.openhealthexchange.openpixpdq.ihe.PdSupplierException;
import org.openhealthexchange.openpixpdq.ihe.pdq.PdqQuery;
import org.openhealthexchange.openpixpdq.ihe.pdq.PdqResult;

/**
 * Implementation for the quering the laika database for retriving the patient demographic
 * for the given crietria
 * @author abhijeetl
 */
public class PdSupplierAdapter implements IPdSupplierAdapter {
    private static final String IDENTIFIER_DOMAIN_IDENTIFIER = "affinity_domain";
    private static final String IN_ = " in ";
    private static final String LIKE_ = " LIKE ";

    private static final String ORDER_BY = " order by pd.id desc";
    private static final String PATIENT_IDENTIFIER = "patient_identifier";
    private static final String STATE = "ad.state";
    private static final Logger log =
            Logger.getLogger(PdSupplierAdapter.class.getName());
    private static final String COUNTRY = "country";
    private static final String CCONTRY = "cn.name";
    private static final String ETHNICITY = "ethnicity";
    private static final String FEMALE = "F";
    private static final String HOME_PHONE = "home_phone";
    private static final String LOWER = "lower(";
    private static final String MALE = "M";
    private static final String MARITIAL_STATUS = "maritial_status";
    private static final String MOBILE_PHONE = "mobile_phone";
    private static final String OR_ = " or ";
    private static final String PREFIX = "name_prefix";
    private static final String OTHER = "UN";
    private static final String AND_ = " and ";
    private static final String ADDRESS1 = "ad.street_address_line_one";
    private static final String ADDRESS2 = "ad.street_address_line_two";
    private static final String ASSIGNING_AUTHORITY = "affinity_domain";
    private static final String PATIENT_INFO_ID = "patient_id";
    private static final String PERSON_IDENTIFIER = "patient_identifier";
    private static final String CITY = "ad.city";
    private static final String DATE_OF_BIRTH = "date_of_birth";
    private static final String GENDER_ID = "gn.code";
    private static final String GENDERSCODE = "gender";
    private static final String VACATION_HOME_PHONE = "vacation_home_phone";
    private static final String WORK_PHONE = "work_phone";
    private static final String FIRST_NAME = "first_name";
    private static final String LAST_NAME = "last_name";
    private static final String POSTAL_CODE = "ad.postal_code";
    private static final String RACE = "race";
    private static final String RELIGION = "religion";
    private static final String SELECT__FROM_PERSON_IDENTIFIERS = "SELECT * FROM patient_identifiers WHERE ";
    private static final String SUFFIX = "name_suffix";
    private static final String SELECT_FROM_PATIENT = "SELECT distinct " +
            "pd.id, pn.name_prefix, pn.first_name, pn.last_name, " +
            "pn.name_suffix, ad.street_address_line_one, " +
            "ad.street_address_line_two, ad.city, ad.state, " +
            "ad.postal_code, cn.name country, gn.name gender, " +
            "ms.name maritial_status, ri.date_of_birth, ri.affinity_domain_id, " +
            "re.name religion, ra.name race, et.name ethnicity, " +
            "tp.home_phone, tp.work_phone, tp.mobile_phone, " +
            "tp.vacation_home_phone, tp.email, tp.url " +
            "FROM " +
            "patients pd, addresses ad, iso_countries cn, " +
            "registration_information ri, genders gn, person_names pn, " +
            "patient_identifiers pi, marital_statuses ms, religions re, " +
            "races ra, ethnicities et, telecoms tp, test_plans vtp " +
            "where " +
            "pd.id = ad.addressable_id and ad.iso_country_id = cn.id and " +
            "pd.id = ri.patient_id and ri.gender_id = gn.id and " +
            "ms.id = ri.marital_status_id and re.id = ri.religion_id and " +
            "ra.id = ri.race_id and et.id = ri.ethnicity_id and " +
            "tp.reachable_id = pd.id and pn.nameable_id <= pd.id and " +
            "pd.id = pi.patient_id and pd.test_plan_id IS NOT NULL " +
            "and pd.test_plan_id = vtp.id and vtp.type = 'PdqQueryPlan' ";

    /**
     *  The PdqSupplierAdapter is the source that provides patient data for a PdSupplier.
     *
     * @author Abhijeet
     * @version 1.0, Feb 03, 2009
     */
    public PdqResult findPatients(PdqQuery query, MessageHeader header)
            throws PdSupplierException {

        log.log(Level.INFO, "Find patient with FirstName=" +
                query.getPersonName().getFirstName() + " and LastName=" +
                query.getPersonName().getLastName());

        // Extracted details from the input query as a where clause
        String whereClause = getPersonClause(query);

        // Get Patient List for the given where clause
        List<Patient> results = findCandidates(whereClause);
        log.log(Level.INFO, "Number of patients found: " + results.size());

        if (results.size() > 0) {
            log.log(Level.INFO, "Patients: " + results.get(0).getPatientName().getFirstName());
        }

        if (results == null) {
            throw new PdSupplierException("No patients found");
        }

        //Converts to Patients List of List
        List<List<Patient>> allPatients = new ArrayList<List<Patient>>();
        for (int i = 0; i < results.size(); i++) {
            List<Patient> patients = new ArrayList<Patient>();
            Patient patient = results.get(i);
            log.log(Level.INFO, "Found patient " + i + ": " + patient.getPatientName().getFirstName() + " " +
                    patient.getPatientName().getLastName());
            patients.add(patient);
            allPatients.add(patients);
        }
        return new PdqResult(allPatients);
    }

    // Finding the patient for the given criteria
    /** /
     * Searched the database and return the patient object for the given criteria
     * @param whereClause
     * @return List of the patient for the query
     */
    private List<Patient> findCandidates(String whereClause) {
        List<Patient> retList = new ArrayList<Patient>();
        String sql = SELECT_FROM_PATIENT + whereClause;

        log.info("SQL for selecting patients: " + sql);
        DbUtils.getInstance().openConnection();
        ResultSet rs = DbUtils.getInstance().executeQuery(sql);

        int i = 0;
        try {
            if (rs.next()) {
                log.info("patients: " + i++);
                // return Patient Object
                Patient patient = new Patient();
                patient.setPatientName(personName(rs));
                patient.setAdministrativeSex(administrativeSex(rs));
                patient.setAddresses(listAddress(rs));
                patient.setBirthDateTime(birthDate(rs));
                patient.setMaritalStatus(rs.getString(MARITIAL_STATUS));
                patient.setReligion(rs.getString(RELIGION));
                patient.setRace(rs.getString(RACE));
                patient.setEthnicGroup(rs.getString(ETHNICITY));
                patient.setPhoneNumbers(listPhoneNumber(rs));
                patient.setPatientIds(listPatientIdentifier(rs));
                retList.add(patient);
            }
        } catch (Exception ex) {
            log.log(Level.ERROR, "SQLException: " + ex.getMessage());
            //ex.printStackTrace();
        } finally{
            DbUtils.getInstance().closeConnection();
        }
        return retList;
    }

    private List<PatientIdentifier> listPatientIdentifier(ResultSet rs) {
        // Patioent Identifiers
        List<PatientIdentifier> patientIds = new ArrayList<PatientIdentifier>();
        try {
            PatientIdentifier patientId = new PatientIdentifier();
            patientId.setId(rs.getString("affinity_domain_id"));
            patientIds.add(patientId);

            String sql = SELECT__FROM_PERSON_IDENTIFIERS + PATIENT_INFO_ID + " = " + rs.getInt("id");
            log.info("SQL for selecting patient identifiers: " + sql);

            DbUtils.getInstance().openConnection();
            ResultSet rs2 = DbUtils.getInstance().executeQuery(sql);
            while (rs2.next()) {
                patientId = new PatientIdentifier();
                patientId.setId(rs2.getString(PERSON_IDENTIFIER));

                String[] identHD = rs2.getString(ASSIGNING_AUTHORITY).split("&");
                Identifier identifier = null;
                if (identHD.length == 3) {
                    if (identHD[0].length() == 0) {
                        identifier = new Identifier(null, identHD[1], identHD[2]);
                    } else {
                        identifier = new Identifier(identHD[0], identHD[1], identHD[2]);
                    }                } else {
                    identifier = new Identifier(identHD[0], "", "");
                }

                log.info("Found patient identifiers: " + patientId.getId() + ";" + identifier.getAuthorityNameString());

                patientId.setAssigningAuthority(identifier);
                patientIds.add(patientId);
            }
        } catch (SQLException ex) {
            log.log(Level.ERROR, "SQLException: " + ex.getMessage());
         } finally{
            DbUtils.getInstance().closeConnection();
       }
        return patientIds;
    }

    private PersonName personName(ResultSet rs) {
        // Name
        PersonName personName = new PersonName();
        try {
            personName.setLastName(rs.getString(LAST_NAME));
            personName.setFirstName(rs.getString(FIRST_NAME));
            personName.setPrefix(rs.getString(PREFIX));
            personName.setSuffix(rs.getString(SUFFIX));

            log.info("Found patient Name: " + personName.getPrefix() + " " +
                    personName.getFirstName() + " " + personName.getLastName() + " " +
                    personName.getSuffix());
        } catch (SQLException ex) {
            log.log(Level.ERROR, "SQLException: " + ex.getMessage());
        }
        return personName;
    }

    private List<PhoneNumber> listPhoneNumber(ResultSet rs) {
        //PhoneNumbers
        List<PhoneNumber> phoneList = new ArrayList<PhoneNumber>();
        try {
            String home_phone = null;
            String work_phone = null;
            String mobile_phone = null;
            String vacation_home_phone = null;

            home_phone = rs.getString(HOME_PHONE);
            work_phone = rs.getString(WORK_PHONE);
            mobile_phone = rs.getString(MOBILE_PHONE);
            vacation_home_phone = rs.getString(VACATION_HOME_PHONE);

            log.info("Found patient phone number: " + home_phone + " " +
                    work_phone + " " + mobile_phone + " " + vacation_home_phone);

            PhoneNumber homePhoneNumber = new PhoneNumber();
            homePhoneNumber.setNumber(home_phone);
            PhoneNumber workPhoneNumber = new PhoneNumber();
            workPhoneNumber.setNumber(work_phone);
            PhoneNumber mobilePhoneNumber = new PhoneNumber();
            mobilePhoneNumber.setNumber(mobile_phone);
            PhoneNumber vacation_homePhoneNumber = new PhoneNumber();
            vacation_homePhoneNumber.setNumber(vacation_home_phone);

            phoneList.add(homePhoneNumber);
            phoneList.add(workPhoneNumber);
            phoneList.add(mobilePhoneNumber);
            phoneList.add(vacation_homePhoneNumber);

        } catch (SQLException ex) {
            log.log(Level.ERROR, "SQLException: " + ex.getMessage());
        }
        return phoneList;
    }

    private List<Address> listAddress(ResultSet rs) {
        //Address
        List<Address> addressList = new ArrayList<Address>();
        try {
            String address1 = null;
            String address2 = null;
            String city = null;
            String postal_code = null;
            String country = null;

            address1 = rs.getString(ADDRESS2);
            address2 = rs.getString(ADDRESS2);
            city = rs.getString(CITY);
            postal_code = rs.getString(POSTAL_CODE);
            country = rs.getString(COUNTRY);

            Address address = new Address();
            address.setAddLine1(address1);
            address.setAddLine2(address2);
            address.setAddCity(city);
            address.setAddZip(postal_code);
            address.setAddCountry(country);

            addressList.add(address);
            log.log(Level.INFO, "Found patinet address" + address1 + " " +
                    address2 + " " + city + " " + postal_code + " " + country);
        } catch (SQLException ex) {
            log.log(Level.ERROR, "SQLException: " + ex.getMessage());
        }
        return addressList;
    }

    private Calendar birthDate(ResultSet rs) {
        //birthDateTime
        Calendar birthDateTime = Calendar.getInstance();
        try {
            birthDateTime.setTime(rs.getDate(DATE_OF_BIRTH));
            log.log(Level.INFO, "Found patinet DOB" + birthDateTime.getTime());
        } catch (SQLException ex) {
            log.log(Level.ERROR, "SQLException: " + ex.getMessage());
        }
        return birthDateTime;
    }

    private SexType administrativeSex(ResultSet rs) {
        // Gender
        SexType administrativeSex = SexType.UNKNOWN;
        try {
            String gender = rs.getString(GENDERSCODE);
            if (gender.equals(FEMALE)) {
                administrativeSex = SexType.FEMALE;
            } else if (gender.equals(MALE)) {
                administrativeSex = SexType.MALE;
            } else if (gender.equals(OTHER)) {
                administrativeSex = SexType.OTHER;
            }
            log.log(Level.INFO, "Found patinet gender" + administrativeSex.name());
        } catch (SQLException ex) {
            log.log(Level.ERROR, "SQLException: " + ex.getMessage());
        }
        return administrativeSex;
    }

    /**
     * Converts from <code>PdqQuery</code> used for where clause
     *
     * @param query the <code>PdqQuery</code> object to be converted from
     * @return a Where clause <code>String</code>
     */
    private String getPersonClause(PdqQuery query) {

        StringBuilder retString = new StringBuilder();

        // Person Name
        getPersonName(query, retString);
        // Gender
        getSex(query, retString);
        // Birth Date
        getBirthDate(query, retString);
        // Address
        getAddress(query, retString);
        // Telephone
        getPhone(query, retString);
        // PatientIdentifier
        getPatientIdentifier(query, retString);

        return retString.toString() + ORDER_BY;
    }

    private void getPersonName(PdqQuery query, StringBuilder retString) {
        // Person Name
        if (query.getPersonName() != null) {
            if (query.getPersonName().getFirstName() != null && query.getPersonName().getFirstName().length() > 0) {
                retString.append(AND_);
                retString.append(LOWER + FIRST_NAME + ") = ");
                retString.append(LOWER + "'" + query.getPersonName().getFirstName() + "')");
            }

            if (query.getPersonName().getLastName() != null && query.getPersonName().getLastName().length() > 0) {
                retString.append(AND_);
                retString.append(LOWER + LAST_NAME + ") = ");
                retString.append(LOWER + "'" + query.getPersonName().getLastName() + "')");
            }
            if (query.getPersonName().getSuffix() != null && query.getPersonName().getSuffix().length() > 0) {
                retString.append(AND_);
                retString.append(LOWER + SUFFIX + ") = ");
                retString.append(LOWER + "'" + query.getPersonName().getSuffix() + "')");
            }
            if (query.getPersonName().getPrefix() != null && query.getPersonName().getPrefix().length() > 0) {
                retString.append(AND_);
                retString.append(LOWER + PREFIX + ") = ");
                retString.append(LOWER + "'" + query.getPersonName().getPrefix() + "')");
            }
        }
    }

    private void getSex(PdqQuery query, StringBuilder retString) {
        // Gender
        if (query.getSex() != null) {
            retString.append(AND_);

            retString.append(GENDER_ID + " = ");
            if (query.getSex() == SexType.MALE) {
                retString.append("'" + MALE + "'");

            } else if (query.getSex() == SexType.FEMALE) {
                retString.append("'" + FEMALE + "'");

            } else if (query.getSex() == SexType.OTHER) {
                retString.append("'" + OTHER + "'");
            }
        }

    }

    private void getBirthDate(PdqQuery query, StringBuilder retString) {
        // Birth Date
        if (query.getBirthDate() != null) {
            if (retString.length() > 0) {
                retString.append(AND_);
            }
            retString.append(DATE_OF_BIRTH + " = ");

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", new Locale("en","US"));
            retString.append("'" + formatter.format(query.getBirthDate().getTime()) + "'");
        }
    }

    private void getAddress(PdqQuery query, StringBuilder retString) {

        // Address
        if (query.getAddress() != null) {

            if (query.getAddress().getAddLine1() != null && query.getAddress().getAddLine1().length() > 0) {
                retString.append(AND_);
                retString.append(LOWER + ADDRESS1 + ")" + LIKE_);
                retString.append(LOWER + "'%" + query.getAddress().getAddLine1() + "%')");
            }

            if (query.getAddress().getAddLine2() != null && query.getAddress().getAddLine2().length() > 0) {
                retString.append(AND_);
                retString.append(LOWER + ADDRESS2 + ")" + LIKE_);
                retString.append(LOWER + "'%" + query.getAddress().getAddLine2() + "%')");
            }

            if (query.getAddress().getAddCity() != null && query.getAddress().getAddCity().length() > 0) {
                retString.append(AND_);
                retString.append(LOWER + CITY + ") = ");
                retString.append(LOWER + "'" + query.getAddress().getAddCity() + "')");
            }
            if (query.getAddress().getAddState() != null && query.getAddress().getAddState().length() > 0) {
                retString.append(AND_);
                retString.append(LOWER + STATE + ") = ");
                retString.append(LOWER + "'" + query.getAddress().getAddState() + "')");
            }
            if (query.getAddress().getAddZip() != null && query.getAddress().getAddZip().length() > 0) {
                retString.append(AND_);
                retString.append(LOWER + POSTAL_CODE + ") = ");
                retString.append(LOWER + "'" + query.getAddress().getAddZip() + "')");
            }
            if (query.getAddress().getAddCountry() != null && query.getAddress().getAddCountry().length() > 0) {
                retString.append(AND_);
                retString.append(LOWER + CCONTRY + ") = ");
                retString.append(LOWER + "'" + query.getAddress().getAddCountry() + "')");
            }
        }
    }

    private void getPhone(PdqQuery query, StringBuilder retString) {
        // Telephobe
        if (query.getPhone() != null) {
            retString.append(AND_);
            if (query.getPhone().getType() == PhoneType.HOME){
                retString.append(HOME_PHONE + " = ");
                retString.append("'"+query.getPhone().getNumber()+"'");
            } else if (query.getPhone().getType() == PhoneType.WORK){
                retString.append(WORK_PHONE + " = ");
                retString.append("'"+query.getPhone().getNumber()+"'");
            } else if (query.getPhone().getType() == PhoneType.CELL){
                retString.append(MOBILE_PHONE + " = ");
                retString.append("'"+query.getPhone().getNumber()+"'");
            } else if (query.getPhone().getType() == PhoneType.EMERGENCY){
                retString.append(VACATION_HOME_PHONE + " = ");
                retString.append("'"+query.getPhone().getNumber()+"'");
            }
        }
    }

    private void getPatientIdentifier(PdqQuery query, StringBuilder retString) {
        // Telephobe
        if (query.getPatientIdentifier() != null) {
            if (query.getPatientIdentifier().getId() != null && query.getPatientIdentifier().getId().length() > 0){
                retString.append(AND_);
                retString.append(PATIENT_IDENTIFIER + " = ");
                retString.append("'"+query.getPatientIdentifier().getId()+"'");
            }
            if (query.getPatientIdentifier().getAssigningAuthority().getAuthorityNameString() != null
                    && query.getPatientIdentifier().getAssigningAuthority().getAuthorityNameString().length() > 0){
                retString.append(AND_);
                retString.append(IDENTIFIER_DOMAIN_IDENTIFIER + LIKE_);
                retString.append("'%"+query.getPatientIdentifier().getAssigningAuthority().getAuthorityNameString()+"%'");
//                retString.append("('"+query.getPatientIdentifier().getAssigningAuthority().getNamespaceId()+"',");
//                retString.append("'&"+query.getPatientIdentifier().getAssigningAuthority().getUniversalId());
//                retString.append("&"+query.getPatientIdentifier().getAssigningAuthority().getUniversalIdType()+"',");
//                retString.append("'"+query.getPatientIdentifier().getAssigningAuthority().getAuthorityNameString()+"')");
            }

        }
    }

    /**
     * This method is not implemented as we are only going to support only QBP^Q21
     * @param queryTag
     * @param messageQueryName
     * @throws org.openhealthexchange.openpixpdq.ihe.PdSupplierException
     */
    public void cancelQuery(String queryTag, String messageQueryName) throws PdSupplierException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}