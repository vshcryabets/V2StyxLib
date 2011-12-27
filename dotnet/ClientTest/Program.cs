using System;
using System.IO;
using StyxLib;
using StyxLib.Messages;
using StyxLib.Messages.Structures;

namespace ClientTest
{
    class Program
    {
        #region Variables
        private StyxClientManager mn;
        #endregion

        public Program()
        {
            mn = new StyxClientManager("127.0.0.1", 564, "user", "/", false);
            checkCreateDirectory();
            checkCreateFile();

        }

        private int checkCreateFile()
        {
            StyxLib.Streams.StyxStream stream = new StyxLib.Streams.StyxStream("/tmp/testfile", FileMode.CreateNew, FileAccess.ReadWrite, mn);
            byte[] array = StyxMessage.MsgEncoding.GetBytes("Hello world!!!");
            stream.Write(array, 0, array.Length);
            stream.Dispose();
            return 0;
        }

        private int checkCreateDirectory()
        {
            StyxLib.FileSystem.StyxDirectoryInfo dirinfo = new StyxLib.FileSystem.StyxDirectoryInfo("/", mn);
            StatStructure [] dirs = dirinfo.GetDirectories();

            dirinfo = new StyxLib.FileSystem.StyxDirectoryInfo("/tmp/", mn);
            if (!dirinfo.Exists) throw new Exception("Dirs /tmp should exist");
            dirinfo = new StyxLib.FileSystem.StyxDirectoryInfo("/tmp/testdir1", mn);
            if (dirinfo.Exists) throw new Exception("Test dir shouldn't exist");
            dirinfo.Create();
            if (!dirinfo.Exists) throw new Exception("Can't create test dir");
            dirinfo.Delete();
            if (dirinfo.Exists) throw new Exception("Can't delete test dir");
            // lets check recursive create
            dirinfo = new StyxLib.FileSystem.StyxDirectoryInfo("/tmp/testdir1/subdir1/subdir2", mn);
            if (dirinfo.Exists) throw new Exception("Test directory 2 shouldn't exist");
            dirinfo.Create(true);
            if (!dirinfo.Exists) throw new Exception("Can't create test dir 2");
            dirinfo.Dispose();
            dirinfo = new StyxLib.FileSystem.StyxDirectoryInfo("/tmp/testdir1", mn);
            dirinfo.Delete(true);
            if (dirinfo.Exists) throw new Exception("Can't delete testdir1");
            return 0;
        }

        static void Main(string[] args)
        {
            try
            {
                new Program();
            }
            catch (StyxLib.StyxErrorMessageException ex)
            {
                Console.WriteLine("Styx error main: {0}\n{1}", ex.ToString(), ex.message.Error);
                Console.ReadKey();
            }
            catch (Exception ex)
            {
                Console.WriteLine("Error main: {0}", ex.ToString());
                Console.ReadKey();
            }
        }
    }
}
