package nessusTools.data.entity;

import com.fasterxml.jackson.databind.annotation.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "License")
@Table(name = "license")
public class License extends GeneratedIdPojo {
	public static final ObjectLookupDao<License> dao
			= new ObjectLookupDao<License>(License.class);

	@Column(name = "`limit`") // tick marks to escape the field name, because 'limit' is a SQL keyword
    @Convert(converter = MultiTypeWrapper.Converter.class)
    @JsonDeserialize(using = MultiTypeWrapper.Deserializer.class)
    @JsonSerialize(using = MultiTypeWrapper.Serializer.class)
	private MultiTypeWrapper limit;

    @Column
    @Convert(converter = MultiTypeWrapper.Converter.class)
    @JsonDeserialize(using = MultiTypeWrapper.Deserializer.class)
    @JsonSerialize(using = MultiTypeWrapper.Serializer.class)
	private MultiTypeWrapper trimmed;

	public MultiTypeWrapper getLimit(){
		return this.limit;
	}

	public MultiTypeWrapper getTrimmed(){
		return this.trimmed;
	}

    public void setLimit(MultiTypeWrapper limit) {
        this.limit = limit;
    }

    public void setTrimmed(MultiTypeWrapper trimmed) {
        this.trimmed = trimmed;
    }

}
