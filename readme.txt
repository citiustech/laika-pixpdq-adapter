To Start OpenPIXPDQ
----------------------

1.  Set the 'JAVA_HOME' variable in the 'startserver.sh' file to point to the correct java version.

2.  To start the server in standard communication mode, append 'standard' to line no 7 in the 'startserver.sh' file

	e.g "%JAVA_HOME%/bin/java" -jar OpenPIXPDQAdapter.jar standard

3.  To start the server in secure communication mode, append 'secure' to line no 7 in the 'startserver.sh' file
	
	e.g "%JAVA_HOME%/bin/java" -jar OpenPIXPDQAdapter.jar secure

4.  To start the server in both standard and secure communication mode, do not append any thing to line no 7 in the 'startserver.sh' file

	e.g "%JAVA_HOME%/bin/java" -jar OpenPIXPDQAdapter.jar

5.  To start the application, run 'startserver.sh'.

6.  'OpenPIXPDQ server started successfully' is displayed on the console when the server is started.

Assumptions
----------------------

1.  Java version 1.6 or higher is installed.

2.  Laika is installed with MySQL database having the default settings.

    a.  schema = laika
    b.  userid = root
    c.  password = <blank>

3.  The database has been migrated to the current version.  

--------------------------------------------------------

If the database configuration is different than what is mentioned above please follow the steps given below before starting OpenPIXPDQ:

1.  Configure the 'property' nodes in the 'hibernate.cfg.xml' in the 'conf' directory to connect to the correct database.

    hibernate.connection.driver_class		database driver name
    hibernate.connection.url			location of the database
    hibernate.connection.username		name of the database user
    hibernate.connection.password		password of the database user
    dialect					The database language dialect

    Sample configuration
    -------------------------------------
    <property name="hibernate.connection.driver_class">com.mysql.jdbc.Driver</property>
    <property name="hibernate.connection.url">jdbc:mysql://localhost:3306/laika</property>
    <property name="hibernate.connection.username">root</property>
    <property name="hibernate.connection.password"></property>
    <property name="hibernate.connection.pool_size">100</property>
    <property name="dialect">org.hibernate.dialect.MySQLDialect</property>


2.  Configure the 'db.properties' file in the 'conf' directory to connect to the correct database.

    a.  url		location of the database
    b.  userid		name of the database user
    c.  password	password of the database user
    d.  driver		database driver name

    Sample configuration
    -------------------------------------
    url=jdbc:mysql://localhost:3306/laika
    userid=root
    password=pwd123
    driver=com.mysql.jdbc.Driver
    -------------------------------------

Ports
------

The default ports for OpenPIXPDQ are

PIX Manager Standard	= 3600
PIX Manager Secure	= 3610
PD Supplier Standard	= 3601
PD Supplier Secure	= 3710

To change this do the following:

To change PIX Manager port

1.  Open the 'PixManagerConnections.xml' in the 'conf/actors' directory.

2.  Change the value in the <port> tag.  There will be one <port> tag for each, standard and secure.
 
To change PD Supplier port

1.  Open the 'PdSupplierConnections.xml' in the 'conf/actors' directory.

2.  Change the value in the <port> tag.  There will be one <port> tag for each, standard and secure.

OpenPIXPDQ needs to be restarted if any configuration changes are made.