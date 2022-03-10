package nessusData.entity;

import java.sql.Timestamp;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import nessusData.persistence.*;
import nessusData.serialize.*;
import nessusData.serialize.EpochTimestampDeserializer;
import nessusData.serialize.EpochTimestampSerializer;
import org.apache.logging.log4j.*;
import javax.persistence.*;

@Entity(name = "Scan")
@Table(name = "scan")
public class Scan implements Pojo {
    public static final Dao<Scan> dao = new Dao<Scan>(Scan.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LogManager.getLogger(Scan.class);

    @Id
    @JsonProperty
    private int id;

    @OneToOne
    @JoinColumn(name = "id")
    @JsonIgnore
    private ScanInfo scanInfo;

    @Column
    private String name;

    @Column
    @JsonProperty
    private String uuid;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="folder_id")
    @JsonProperty("folder_id")
    @JsonDeserialize(using = IdRefDeserializer.class)
    @JsonSerialize(using = IdRefSerializer.class)
    private Folder folder;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="owner_id")
    @JsonDeserialize(using = LookupDeserializer.class)
    @JsonSerialize(using = LookupSerializer.class)
    private ScanOwner owner;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="type_id")
    @JsonDeserialize(using = LookupDeserializer.class)
    @JsonSerialize(using = LookupSerializer.class)
    private ScanType type;

    @Column
    private String rrules;

    @Column(name = "`read`")
    private boolean read;

    @Column
    private boolean shared;

    @Column
    private boolean enabled;

    @Column
    private boolean control;

    @Column(name = "user_permissions")
    @JsonProperty("user_permissions")
    private Integer userPermissions;

    @Column
    private String status;

    @Column(name = "creation_date")
    @JsonProperty("creation_date")
    @JsonDeserialize(using = EpochTimestampDeserializer.class)
    @JsonSerialize(using = EpochTimestampSerializer.class)
    private Timestamp creationDate;

    @Column(name = "start_time")
    @JsonProperty("starttime")
    private String startTime;

    @Column(name = "last_modification_date")
    @JsonProperty("last_modification_date")
    @JsonDeserialize(using = EpochTimestampDeserializer.class)
    @JsonSerialize(using = EpochTimestampSerializer.class)
    private Timestamp lastModificationDate;

    @ManyToOne
    @JoinColumn(
            name="timezone_id",
            foreignKey = @ForeignKey(name = "scan_timezone_id_fk")
    )
    @JsonDeserialize(using = LookupDeserializer.class)
    @JsonSerialize(using = LookupSerializer.class)
    private Timezone timezone;

    @Column(name = "live_results")
    @JsonProperty("live_results")
    private Integer liveResults;


    public Scan() { }

    public boolean equals(Object o) {
        return this._equals(o);
    }

    public String toString() {
        try {
            return this.toJson();
        } catch (JsonProcessingException e) {
            return super.toString()
                    + " toString() could not convert to JSON: "
                    + e.getMessage();
        }
    }


/**********************************************
      Standard getters/setters below
***********************************************/

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public ScanInfo getScanInfo() {
        return scanInfo;
    }

    public void setScanInfo(ScanInfo scanInfo) {
        this.scanInfo = scanInfo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public ScanOwner getOwner() {
        return owner;
    }

    public void setOwner(ScanOwner owner) {
        this.owner = owner;
    }

    public ScanType getType() {
        return type;
    }

    public void setType(ScanType type) {
        this.type = type;
    }

    public String getRrules() {
        return rrules;
    }

    public void setRrules(String rrules) {
        this.rrules = rrules;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isControl() {
        return control;
    }

    public void setControl(boolean control) {
        this.control = control;
    }

    public Integer getUserPermissions() {
        return userPermissions;
    }

    public void setUserPermissions(Integer userPermissions) {
        this.userPermissions = userPermissions;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public Timestamp getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(Timestamp lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public Timezone getTimezone() {
        return timezone;
    }

    public void setTimezone(Timezone timezone) {
        this.timezone = timezone;
    }

    public Integer getLiveResults() {
        return liveResults;
    }

    public void setLiveResults(Integer liveResults) {
        this.liveResults = liveResults;
    }
}