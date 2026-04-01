package psp.taxcredits.model {
    import flash.utils.ByteArray;
    import mx.utils.Base64Encoder;
    import mx.utils.Base64Decoder;

    public class AMFSerializer {
        public function serializeToString(value:Object):String{
            if(value==null){
                throw new Error("null isn't a legal serialization candidate");
            }
            var bytes:ByteArray = new ByteArray();
            bytes.writeObject(value);
            bytes.position = 0;
            var be:Base64Encoder = new Base64Encoder();
            be.encodeBytes(bytes);
            var res:String = be.toString();
            be.reset();
            return res;
        }

        public function readObjectFromStringBytes(value:String):Object{
            var dec:Base64Decoder=new Base64Decoder();
            dec.decode(value);
            var result:ByteArray=dec.drain();
            result.position=0;
            return result.readObject();
        }
    }
}