package za.org.grassroot2.database;

import junit.framework.TestCase;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import za.org.grassroot2.GrassrootApplication;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.MediaFile;
import za.org.grassroot2.model.UserProfile;
import za.org.grassroot2.model.alert.LiveWireAlert;


/**
 * Created by qbasso on 19.09.2017.
 */

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 25, application = GrassrootApplication.class, manifest = Config.NONE)
public class DatabaseServiceTest extends TestCase{

    private DatabaseService databaseService;

    private final String TEST_DB_NAME = "grassroot.test";

    public DatabaseServiceTest() {
        databaseService = new DatabaseServiceImpl(new DatabaseHelper(RuntimeEnvironment.application.getApplicationContext(), TEST_DB_NAME));
        assertNotNull(databaseService);
    }

    @org.junit.Test
    public void userProfileCRUD() throws Exception {
        databaseService.updateOrCreateUserProfile(UUID.randomUUID().toString(), "1", "John", "Admin");
        UserProfile profile = databaseService.loadUserProfile();
        assertEquals(profile.getDisplayName(), "John");
        databaseService.updateOrCreateUserProfile(profile.getUid(), "1", "Johnas", "Admin");
        profile = databaseService.loadUserProfile();
        assertEquals(profile.getDisplayName(), "Johnas");
    }


    @org.junit.Test
    public void loadGroup() throws Exception {
        Group g = createGroup(UUID.randomUUID().toString());
        databaseService.storeObject(Group.class, g);
        Group loadedGroup = databaseService.loadGroup(g.getUid().toString());
        assertEquals(g.getName(), loadedGroup.getName());
    }

    @org.junit.Test
    public void loadObjectByUid() throws Exception {
        LiveWireAlert alert = new LiveWireAlert();
        alert.setDescription("Test description");
        alert.setGroupUid("1");
        databaseService.storeObject(LiveWireAlert.class, alert);
        LiveWireAlert loadedAlert = databaseService.loadObjectByUid(LiveWireAlert.class, alert.getUid().toString());
        assertEquals(alert.getUid(), loadedAlert.getUid());

        MediaFile file = new MediaFile("path", "path", "image/", "thumbnail");
        databaseService.storeObject(MediaFile.class, file);
        MediaFile loadedMediaFile = databaseService.loadObjectByUid(MediaFile.class, file.getUid().toString());
        assertEquals(file.getUid(), loadedMediaFile.getUid());
    }

    @org.junit.Test
    public void loadExistingObjectsWithLastChangeTime() throws Exception {
        Group g = createGroup(UUID.randomUUID().toString());
        databaseService.storeObject(Group.class, g);
        Map<String, Long> result = databaseService.loadExistingObjectsWithLastChangeTime(Group.class);
        assertEquals(result.size(), 1);
        assertTrue(result.get(g.getUid())>0);
    }

    @org.junit.Test
    public void copyOrUpdateListOfEntities() throws Exception {
        Group g1 = createGroup("Name 1");
        Group g2 = createGroup("Name 2");
        databaseService.store(Group.class,g1);
        databaseService.store(Group.class,g1);
        List<Group> items = new ArrayList<>();
        items.add(g1);
        items.add(g2);
        g1.setName("Name 3");
        databaseService.copyOrUpdateListOfEntities(Group.class, items);
        Group changedGroup = databaseService.loadObjectByUid(Group.class, g1.getUid().toString());
        assertNotSame(g1.getName(), changedGroup.getName());
    }

    @org.junit.Test
    public void removeUserProfile() throws Exception {
        databaseService.updateOrCreateUserProfile(UUID.randomUUID().toString(), "1", "John", "Admin");
        databaseService.removeUserProfile();
        assertNull(databaseService.loadUserProfile());
    }

    private Group createGroup(String name) {
        Group g = new Group();
        g.setName(name);
        g.setUid(UUID.randomUUID());
        g.setMemberCount(10);
        g.setUserRole("Admin");
        g.setLastActionOrChange(System.currentTimeMillis());
        g.setLastTimeChangedServer(System.currentTimeMillis());
        return g;
    }

}
