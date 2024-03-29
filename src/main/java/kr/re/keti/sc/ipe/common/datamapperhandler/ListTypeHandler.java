package kr.re.keti.sc.ipe.common.datamapperhandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Array 형태의 DB 컬럼에 데이터 입력/조회를 위한 TypeHandler 클래스
 */
public class ListTypeHandler extends BaseTypeHandler<Object> {

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, Object o, JdbcType jdbcType) throws SQLException {
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {

        Array array = rs.getArray(columnName);
        List items = new ArrayList<>();

        if (!rs.wasNull()) {

            Object[] arrayItems = (Object[]) array.getArray();

            for (int i = 0; i < arrayItems.length; i++) {
                items.add(arrayItems[i]);
            }

            return items;
        } else {
            return null;
        }
    }

    @Override
    public Object getNullableResult(ResultSet resultSet, int i) throws SQLException {
        return null;
    }

    @Override
    public Object getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        return null;
    }
}
