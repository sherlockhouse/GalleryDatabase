package com.freeme.greendaogenerator;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * Created by connorlin on 16-1-18.
 */
public class ExampleDaoGenerator {
    public static void main(String[] args) throws Exception {
        // 正如你所见的，你创建了一个用于添加实体（Entity）的模式（Schema）对象。
        // 两个参数分别代表：数据库版本号与自动生成代码的包路径。
        Schema schema = new Schema(1, "com.freeme.connorlin");
//      当然，如果你愿意，你也可以分别指定生成的 Bean 与 DAO 类所在的目录，只要如下所示：
//      Schema schema = new Schema(1, "me.itangqi.bean");
//      schema.setDefaultJavaPackageDao("me.itangqi.dao");

        // 模式（Schema）同时也拥有两个默认的 flags，分别用来标示 entity 是否是 activie 以及是否使用 keep sections。
        // schema2.enableActiveEntitiesByDefault();
        // schema2.enableKeepSectionsByDefault();

        // 一旦你拥有了一个 Schema 对象后，你便可以使用它添加实体（Entities）了。
        addNote(schema);

        // 最后我们将使用 DAOGenerator 类的 generateAll() 方法自动生成代码，此处你需要根据自己的情况更改输出目录（既之前创建的 java-gen)。
        // 其实，输出目录的路径可以在 build.gradle 中设置，有兴趣的朋友可以自行搜索，这里就不再详解。
        new DaoGenerator().generateAll(schema, "/home/connorlin/AndroidStudio_Projects/FreemeGallery/app/src/main/freeme");
    }

    /**
     * @param schema
     */
    private static void addNote(Schema schema) {
        // 一个实体（类）就关联到数据库中的一张表，此处表名为「Note」（既类名）
        Entity note = schema.addEntity("GalleryFiles");
        // 你也可以重新给表命名
        // note.setTableName("NODE");

        // greenDAO 会自动根据实体类的属性值来创建表字段，并赋予默认值
        // 接下来你便可以设置表中的字段：
        note.addIdProperty();
        // 与在 Java 中使用驼峰命名法不同，默认数据库中的命名是使用大写和下划线来分割单词的。
        // For example, a property called “creationDate” will become a database column “CREATION_DATE”.
        note.addStringProperty("data").columnName("_data").notNull();
        note.addIntProperty("size").columnName("_size");
        note.addIntProperty("media_type").columnName("media_type");
        note.addStringProperty("display_name").columnName("_display_name");
        note.addStringProperty("mime_type").columnName("mime_type");
        note.addStringProperty("title").columnName("title");
        note.addIntProperty("date_added").columnName("date_added");
        note.addIntProperty("date_modified").columnName("date_modified");
        note.addStringProperty("description").columnName("description");
        note.addStringProperty("picasa_id").columnName("picasa_id");
        note.addIntProperty("duration").columnName("duration");
        note.addStringProperty("artist").columnName("artist");
        note.addStringProperty("album").columnName("album");
        note.addStringProperty("resolution").columnName("resolution");
        note.addIntProperty("width").columnName("width");
        note.addIntProperty("height").columnName("height");
        note.addDoubleProperty("latitude").columnName("latitude");
        note.addDoubleProperty("longitude").columnName("longitude");
        note.addIntProperty("datetaken").columnName("datetaken");
        note.addIntProperty("orientation").columnName("orientation");
        note.addIntProperty("mini_thumb_magic").columnName("mini_thumb_magic");
        note.addStringProperty("bucket_id").columnName("bucket_id");
        note.addStringProperty("bucket_display_name").columnName("bucket_display_name");
        note.addIntProperty("story_bucket_id").columnName("story_bucket_id");
        note.addIntProperty("is_hidden").columnName("is_hidden");
        note.addStringProperty("lbs_loc").columnName("lbs_loc");
    }
}
