package ru.ashepelev;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Edge {
    @JacksonXmlProperty
    public String source;
    @JacksonXmlProperty
    public String target;
}
