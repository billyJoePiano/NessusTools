package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.SimpleStringLookupPojo;
import nessusTools.data.persistence.SimpleStringLookupDao;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Represents a simple string/varchar lookup from the scanner table
 */
@Entity(name = "Scanner")
@Table(name = "scanner")
public class Scanner extends SimpleStringLookupPojo<Scanner> {
    public static final SimpleStringLookupDao<Scanner> dao
            = new SimpleStringLookupDao<Scanner>(Scanner.class);

}