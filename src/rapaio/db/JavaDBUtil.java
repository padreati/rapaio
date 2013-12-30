
package rapaio.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import rapaio.data.Frame;
import rapaio.data.NominalVector;
import rapaio.data.NumericVector;
import rapaio.data.SolidFrame;
import rapaio.data.Vector;

/**
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class JavaDBUtil {

    private Connection conn;

    public void connect() throws SQLException, ClassNotFoundException {
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        conn = DriverManager.getConnection("jdbc:derby:memory:m;create=true");
    }

    public void putFrame(Frame df, String tableName) throws SQLException {
        String[] columns = df.getColNames();
        String[] types = new String[columns.length];
        for (int i = 0; i < types.length; i++) {
            if (df.getCol(columns[i]).isNumeric()) {
                types[i] = "DOUBLE";
            }
            if (df.getCol(columns[i]).isNominal()) {
                types[i] = "VARCHAR(8000)";
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(tableName).append(" (");
        for (int i = 0; i < columns.length; i++) {
            sb.append(columns[i]).append(" ").append(types[i]);
            if (i != columns.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sb.toString());
        }

        sb = new StringBuilder();
        sb.append("INSERT INTO ").append(tableName).append(" (");
        for (int i = 0; i < columns.length; i++) {
            sb.append(columns[i]);
            if (i != columns.length - 1) {
                sb.append(",");
            }
        }
        sb.append(") VALUES (");
        for (int i = 0; i < columns.length; i++) {
            sb.append("?");
            if (i != columns.length - 1) {
                sb.append(",");
            }
        }
        sb.append(")");
        try (PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            for (int i = 0; i < df.getRowCount(); i++) {
                for (int j = 0; j < types.length; j++) {
                    switch (types[i]) {
                        case "VARCHAR(8000)":
                            ps.setString(j + 1, df.getLabel(i, j));
                            break;
                        case "DOUBLE":
                            ps.setDouble(j + 1, df.getValue(i, j));
                            break;
                    }
                }
                ps.execute();
            }
        }
    }

    public Frame getFrame(String query) throws SQLException {
        Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery(query);

        ResultSetMetaData md = rs.getMetaData();
        List<List> lists = new ArrayList<>();
        for (int i = 0; i < md.getColumnCount(); i++) {
            lists.add(new ArrayList());
        }
        while (rs.next()) {
            for (int i = 0; i < md.getColumnCount(); i++) {
                String sqlTypeName = md.getColumnTypeName(i);
                switch (sqlTypeName) {
                    case "DOUBLE":
                        lists.get(i).add(rs.getDouble(i + 1));
                        break;
                    default:
                        lists.get(i).add(rs.getString(i + 1));
                }
            }
        }
        List<Vector> vectors = new ArrayList<>();
        List<String> colNames = new ArrayList();
        for (int i = 0; i < md.getColumnCount(); i++) {
            String sqlTypeName = md.getColumnTypeName(i);
            switch (sqlTypeName) {
                case "DOUBLE":
                    NumericVector v1 = new NumericVector(lists.get(i).size());
                    for (int j = 0; j < lists.get(i).size(); j++) {
                        v1.setValue(j, (Double) lists.get(i).get(j));
                    }
                    vectors.add(v1);
                    break;
                default:
                    HashSet<String> dict = new HashSet<>();
                    for (int j = 0; j < lists.get(i).size(); j++) {
                        dict.add((String) lists.get(i).get(j));
                    }
                    NominalVector v2 = new NominalVector(lists.get(i).size(), dict);
                    for (int j = 0; j < lists.get(i).size(); j++) {
                        v2.setLabel(j, (String) lists.get(i).get(j));
                    }
                    lists.get(i).add(v2);
            }
        }
        return new SolidFrame(lists.get(0).size(), vectors, colNames);
    }
}
