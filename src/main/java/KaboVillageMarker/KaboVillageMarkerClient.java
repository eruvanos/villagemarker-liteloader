package KaboVillageMarker;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class KaboVillageMarkerClient {

    public static KaboVillageMarkerClient instance = new KaboVillageMarkerClient();
    @SuppressWarnings("unchecked")
    private List<KaboVillageMarkerClient.KaboVillageMarkerVillage>[] villages = (List<KaboVillageMarkerClient.KaboVillageMarkerVillage>[]) new ArrayList[]{new ArrayList<KaboVillageMarkerClient.KaboVillageMarkerVillage>(), new ArrayList<KaboVillageMarkerClient.KaboVillageMarkerVillage>(), new ArrayList<KaboVillageMarkerClient.KaboVillageMarkerVillage>()};
    @SuppressWarnings("unchecked")
    private List<String>[] bufferedStringsByDimension = (List<String>[]) new ArrayList[]{new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>()};
    private int[] currentIDByDimension = new int[]{1000, 1000, 1000};


    public List<KaboVillageMarkerClient.KaboVillageMarkerVillage> getVillageListForDimension(int dimension) {
        return this.villages[this.dimensionToIndex(dimension)];
    }

    public void bufferVillageDataString(String dataString) {
        int id = Integer.parseInt(dataString.split("<")[0]);
        int dim = Integer.parseInt(dataString.split(">")[0].split("<")[1].split(":")[0]);
        int size = Integer.parseInt(dataString.split(">")[0].split("<")[1].split(":")[2]);
        int part = Integer.parseInt(dataString.split(">")[0].split("<")[1].split(":")[1]);
        // System.out.println("id : "+id+"\tdim : "+dim+"\tsize : "+size+"\tpart : "+part);
        boolean notComplete = false;
        String data = dataString.split(">")[1];
        if (this.currentIDByDimension[this.dimensionToIndex(dim)] == id) {
            this.bufferedStringsByDimension[this.dimensionToIndex(dim)].set(part - 1, data);
        } else {
            this.currentIDByDimension[this.dimensionToIndex(dim)] = id;
            this.bufferedStringsByDimension[this.dimensionToIndex(dim)].clear();

            for (int toTranslator = 0; toTranslator < size; ++toTranslator) {
                this.bufferedStringsByDimension[this.dimensionToIndex(dim)].add(null);
            }

            this.bufferedStringsByDimension[this.dimensionToIndex(dim)].set(part - 1, data);
        }

        for(String s : this.bufferedStringsByDimension[this.dimensionToIndex(dim)]) {
            if(s == null) notComplete = true;
        }



        if (!notComplete) {
            String var13 = "";

            Object bufferedString;
            for (Iterator var12 = this.bufferedStringsByDimension[this.dimensionToIndex(dim)].iterator(); var12.hasNext(); var13 = var13 + (String)bufferedString) {
                bufferedString = var12.next();
            }

            this.translateStringToVillageData(var13);
        }

    }

    public void translateStringToVillageData(String dataString) {
        String[] villageStrings = dataString.split(":");
        List<KaboVillageMarkerClient.KaboVillageMarkerVillage> translatedVillages = new ArrayList<KaboVillageMarkerClient.KaboVillageMarkerVillage>();
        int dimension = Integer.parseInt(villageStrings[0]);

        for (int i = 1; i < villageStrings.length; ++i) {
            String[] doors = villageStrings[i].split(";");
            KaboVillageMarkerClient.KaboVillageMarkerVillage village = new KaboVillageMarkerClient.KaboVillageMarkerVillage();
            village.radius = Integer.parseInt(doors[0]);
            String[] coords = doors[1].split(",");
            village.x = Integer.parseInt(coords[0]);
            village.y = Integer.parseInt(coords[1]);
            village.z = Integer.parseInt(coords[2]);

            for (int j = 2; j < doors.length; ++j) {
                coords = doors[j].split(",");
                KaboVillageMarkerClient.KaboVillageMarkerVillageDoorPosition doorPosition = new KaboVillageMarkerClient.KaboVillageMarkerVillageDoorPosition(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
                village.villageDoors.add(doorPosition);
            }

            translatedVillages.add(village);
        }

        this.villages[this.dimensionToIndex(dimension)] = translatedVillages;
    }

    private int dimensionToIndex(int dimension) {
        return dimension == -1 ? 1 : (dimension == 1 ? 2 : 0);
    }


    public class KaboVillageMarkerVillage {

        public int radius;
        public int x;
        public int y;
        public int z;
        public List<KaboVillageMarkerClient.KaboVillageMarkerVillageDoorPosition> villageDoors = new ArrayList<KaboVillageMarkerClient.KaboVillageMarkerVillageDoorPosition>();


        public void clearDoorPositions() {
            this.villageDoors.clear();
        }

        public void addDoorPosition(int doorX, int doorY, int doorZ) {
            this.villageDoors.add(KaboVillageMarkerClient.this.new KaboVillageMarkerVillageDoorPosition(doorX, doorY, doorZ));
        }

        public BlockPos getCenter() {
            return new BlockPos(this.x, this.y, this.z);
        }
    }

    public class KaboVillageMarkerVillageDoorPosition {

        public int x;
        public int y;
        public int z;


        public KaboVillageMarkerVillageDoorPosition(int doorX, int doorY, int doorZ) {
            this.x = doorX;
            this.y = doorY;
            this.z = doorZ;
        }
    }
}
