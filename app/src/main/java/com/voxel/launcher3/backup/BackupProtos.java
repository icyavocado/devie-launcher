package com.voxel.launcher3.backup;

import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.InternalNano;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.WireFormatNano;
import java.io.IOException;
import java.util.Arrays;

/* JADX INFO: loaded from: classes.dex */
public interface BackupProtos {

    public static final class CheckedMessage extends MessageNano {
        public long checksum;
        public byte[] payload;

        public CheckedMessage() {
            clear();
        }

        public CheckedMessage clear() {
            this.payload = WireFormatNano.EMPTY_BYTES;
            this.checksum = 0L;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.google.protobuf.nano.MessageNano
        protected int computeSerializedSize() {
            return super.computeSerializedSize() + CodedOutputByteBufferNano.computeBytesSize(1, this.payload) + CodedOutputByteBufferNano.computeInt64Size(2, this.checksum);
        }

        @Override // com.google.protobuf.nano.MessageNano
        public CheckedMessage mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            while (true) {
                int tag = codedInputByteBufferNano.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    this.payload = codedInputByteBufferNano.readBytes();
                } else if (tag == 16) {
                    this.checksum = codedInputByteBufferNano.readInt64();
                } else if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, tag)) {
                    return this;
                }
            }
        }

        @Override // com.google.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            codedOutputByteBufferNano.writeBytes(1, this.payload);
            codedOutputByteBufferNano.writeInt64(2, this.checksum);
            super.writeTo(codedOutputByteBufferNano);
        }
    }

    public static final class Favorite extends MessageNano {
        public int appWidgetId;
        public String appWidgetProvider;
        public int cellX;
        public int cellY;
        public int container;
        public int displayMode;
        public byte[] icon;
        public String iconPackage;
        public String iconResource;
        public int iconType;
        public long id;
        public String intent;
        public int itemType;
        public int screen;
        public int spanX;
        public int spanY;
        public String title;
        public String uri;

        public Favorite() {
            clear();
        }

        public Favorite clear() {
            this.id = 0L;
            this.itemType = 0;
            this.title = "";
            this.container = 0;
            this.screen = 0;
            this.cellX = 0;
            this.cellY = 0;
            this.spanX = 0;
            this.spanY = 0;
            this.displayMode = 0;
            this.appWidgetId = 0;
            this.appWidgetProvider = "";
            this.intent = "";
            this.uri = "";
            this.iconType = 0;
            this.iconPackage = "";
            this.iconResource = "";
            this.icon = WireFormatNano.EMPTY_BYTES;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.google.protobuf.nano.MessageNano
        protected int computeSerializedSize() {
            int iComputeSerializedSize = super.computeSerializedSize() + CodedOutputByteBufferNano.computeInt64Size(1, this.id) + CodedOutputByteBufferNano.computeInt32Size(2, this.itemType);
            if (!this.title.equals("")) {
                iComputeSerializedSize += CodedOutputByteBufferNano.computeStringSize(3, this.title);
            }
            int i = this.container;
            if (i != 0) {
                iComputeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(4, i);
            }
            int i2 = this.screen;
            if (i2 != 0) {
                iComputeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(5, i2);
            }
            int i3 = this.cellX;
            if (i3 != 0) {
                iComputeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(6, i3);
            }
            int i4 = this.cellY;
            if (i4 != 0) {
                iComputeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(7, i4);
            }
            int i5 = this.spanX;
            if (i5 != 0) {
                iComputeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(8, i5);
            }
            int i6 = this.spanY;
            if (i6 != 0) {
                iComputeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(9, i6);
            }
            int i7 = this.displayMode;
            if (i7 != 0) {
                iComputeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(10, i7);
            }
            int i8 = this.appWidgetId;
            if (i8 != 0) {
                iComputeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(11, i8);
            }
            if (!this.appWidgetProvider.equals("")) {
                iComputeSerializedSize += CodedOutputByteBufferNano.computeStringSize(12, this.appWidgetProvider);
            }
            if (!this.intent.equals("")) {
                iComputeSerializedSize += CodedOutputByteBufferNano.computeStringSize(13, this.intent);
            }
            if (!this.uri.equals("")) {
                iComputeSerializedSize += CodedOutputByteBufferNano.computeStringSize(14, this.uri);
            }
            int i9 = this.iconType;
            if (i9 != 0) {
                iComputeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(15, i9);
            }
            if (!this.iconPackage.equals("")) {
                iComputeSerializedSize += CodedOutputByteBufferNano.computeStringSize(16, this.iconPackage);
            }
            if (!this.iconResource.equals("")) {
                iComputeSerializedSize += CodedOutputByteBufferNano.computeStringSize(17, this.iconResource);
            }
            return !Arrays.equals(this.icon, WireFormatNano.EMPTY_BYTES) ? iComputeSerializedSize + CodedOutputByteBufferNano.computeBytesSize(18, this.icon) : iComputeSerializedSize;
        }

        @Override // com.google.protobuf.nano.MessageNano
        public Favorite mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            while (true) {
                int tag = codedInputByteBufferNano.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.id = codedInputByteBufferNano.readInt64();
                        break;
                    case 16:
                        this.itemType = codedInputByteBufferNano.readInt32();
                        break;
                    case 26:
                        this.title = codedInputByteBufferNano.readString();
                        break;
                    case 32:
                        this.container = codedInputByteBufferNano.readInt32();
                        break;
                    case 40:
                        this.screen = codedInputByteBufferNano.readInt32();
                        break;
                    case 48:
                        this.cellX = codedInputByteBufferNano.readInt32();
                        break;
                    case 56:
                        this.cellY = codedInputByteBufferNano.readInt32();
                        break;
                    case 64:
                        this.spanX = codedInputByteBufferNano.readInt32();
                        break;
                    case 72:
                        this.spanY = codedInputByteBufferNano.readInt32();
                        break;
                    case 80:
                        this.displayMode = codedInputByteBufferNano.readInt32();
                        break;
                    case 88:
                        this.appWidgetId = codedInputByteBufferNano.readInt32();
                        break;
                    case 98:
                        this.appWidgetProvider = codedInputByteBufferNano.readString();
                        break;
                    case 106:
                        this.intent = codedInputByteBufferNano.readString();
                        break;
                    case 114:
                        this.uri = codedInputByteBufferNano.readString();
                        break;
                    case 120:
                        this.iconType = codedInputByteBufferNano.readInt32();
                        break;
                    case 130:
                        this.iconPackage = codedInputByteBufferNano.readString();
                        break;
                    case 138:
                        this.iconResource = codedInputByteBufferNano.readString();
                        break;
                    case 146:
                        this.icon = codedInputByteBufferNano.readBytes();
                        break;
                    default:
                        if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, tag)) {
                            return this;
                        }
                        break;
                }
            }
        }

        @Override // com.google.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            codedOutputByteBufferNano.writeInt64(1, this.id);
            codedOutputByteBufferNano.writeInt32(2, this.itemType);
            if (!this.title.equals("")) {
                codedOutputByteBufferNano.writeString(3, this.title);
            }
            int i = this.container;
            if (i != 0) {
                codedOutputByteBufferNano.writeInt32(4, i);
            }
            int i2 = this.screen;
            if (i2 != 0) {
                codedOutputByteBufferNano.writeInt32(5, i2);
            }
            int i3 = this.cellX;
            if (i3 != 0) {
                codedOutputByteBufferNano.writeInt32(6, i3);
            }
            int i4 = this.cellY;
            if (i4 != 0) {
                codedOutputByteBufferNano.writeInt32(7, i4);
            }
            int i5 = this.spanX;
            if (i5 != 0) {
                codedOutputByteBufferNano.writeInt32(8, i5);
            }
            int i6 = this.spanY;
            if (i6 != 0) {
                codedOutputByteBufferNano.writeInt32(9, i6);
            }
            int i7 = this.displayMode;
            if (i7 != 0) {
                codedOutputByteBufferNano.writeInt32(10, i7);
            }
            int i8 = this.appWidgetId;
            if (i8 != 0) {
                codedOutputByteBufferNano.writeInt32(11, i8);
            }
            if (!this.appWidgetProvider.equals("")) {
                codedOutputByteBufferNano.writeString(12, this.appWidgetProvider);
            }
            if (!this.intent.equals("")) {
                codedOutputByteBufferNano.writeString(13, this.intent);
            }
            if (!this.uri.equals("")) {
                codedOutputByteBufferNano.writeString(14, this.uri);
            }
            int i9 = this.iconType;
            if (i9 != 0) {
                codedOutputByteBufferNano.writeInt32(15, i9);
            }
            if (!this.iconPackage.equals("")) {
                codedOutputByteBufferNano.writeString(16, this.iconPackage);
            }
            if (!this.iconResource.equals("")) {
                codedOutputByteBufferNano.writeString(17, this.iconResource);
            }
            if (!Arrays.equals(this.icon, WireFormatNano.EMPTY_BYTES)) {
                codedOutputByteBufferNano.writeBytes(18, this.icon);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }

    public static final class Journal extends MessageNano {
        public int appVersion;
        public long bytes;
        public Key[] key;
        public int rows;
        public long t;

        public Journal() {
            clear();
        }

        public Journal clear() {
            this.appVersion = 0;
            this.t = 0L;
            this.bytes = 0L;
            this.rows = 0;
            this.key = Key.emptyArray();
            this.cachedSize = -1;
            return this;
        }

        @Override // com.google.protobuf.nano.MessageNano
        protected int computeSerializedSize() {
            int iComputeSerializedSize = super.computeSerializedSize() + CodedOutputByteBufferNano.computeInt32Size(1, this.appVersion) + CodedOutputByteBufferNano.computeInt64Size(2, this.t);
            long j = this.bytes;
            if (j != 0) {
                iComputeSerializedSize += CodedOutputByteBufferNano.computeInt64Size(3, j);
            }
            int i = this.rows;
            if (i != 0) {
                iComputeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(4, i);
            }
            Key[] keyArr = this.key;
            if (keyArr != null && keyArr.length > 0) {
                for (Key key : keyArr) {
                    if (key != null) {
                        iComputeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(5, key);
                    }
                }
            }
            return iComputeSerializedSize;
        }

        @Override // com.google.protobuf.nano.MessageNano
        public Journal mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            while (true) {
                int tag = codedInputByteBufferNano.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.appVersion = codedInputByteBufferNano.readInt32();
                } else if (tag == 16) {
                    this.t = codedInputByteBufferNano.readInt64();
                } else if (tag == 24) {
                    this.bytes = codedInputByteBufferNano.readInt64();
                } else if (tag == 32) {
                    this.rows = codedInputByteBufferNano.readInt32();
                } else if (tag == 42) {
                    int repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 42);
                    Key[] keyArr = this.key;
                    int length = keyArr == null ? 0 : keyArr.length;
                    Key[] keyArr2 = new Key[repeatedFieldArrayLength + length];
                    if (length != 0) {
                        System.arraycopy(this.key, 0, keyArr2, 0, length);
                    }
                    while (length < keyArr2.length - 1) {
                        keyArr2[length] = new Key();
                        codedInputByteBufferNano.readMessage(keyArr2[length]);
                        codedInputByteBufferNano.readTag();
                        length++;
                    }
                    keyArr2[length] = new Key();
                    codedInputByteBufferNano.readMessage(keyArr2[length]);
                    this.key = keyArr2;
                } else if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, tag)) {
                    return this;
                }
            }
        }

        @Override // com.google.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            codedOutputByteBufferNano.writeInt32(1, this.appVersion);
            codedOutputByteBufferNano.writeInt64(2, this.t);
            long j = this.bytes;
            if (j != 0) {
                codedOutputByteBufferNano.writeInt64(3, j);
            }
            int i = this.rows;
            if (i != 0) {
                codedOutputByteBufferNano.writeInt32(4, i);
            }
            Key[] keyArr = this.key;
            if (keyArr != null && keyArr.length > 0) {
                for (Key key : keyArr) {
                    if (key != null) {
                        codedOutputByteBufferNano.writeMessage(5, key);
                    }
                }
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }

    public static final class Key extends MessageNano {
        private static volatile Key[] _emptyArray;
        public long checksum;
        public long id;
        public String name;
        public int type;

        public Key() {
            clear();
        }

        public static Key[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new Key[0];
                    }
                }
            }
            return _emptyArray;
        }

        public static Key parseFrom(byte[] bArr) throws InvalidProtocolBufferNanoException {
            return (Key) MessageNano.mergeFrom(new Key(), bArr);
        }

        public Key clear() {
            this.type = 1;
            this.name = "";
            this.id = 0L;
            this.checksum = 0L;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.google.protobuf.nano.MessageNano
        protected int computeSerializedSize() {
            int iComputeSerializedSize = super.computeSerializedSize() + CodedOutputByteBufferNano.computeInt32Size(1, this.type);
            if (!this.name.equals("")) {
                iComputeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.name);
            }
            long j = this.id;
            if (j != 0) {
                iComputeSerializedSize += CodedOutputByteBufferNano.computeInt64Size(3, j);
            }
            long j2 = this.checksum;
            return j2 != 0 ? iComputeSerializedSize + CodedOutputByteBufferNano.computeInt64Size(4, j2) : iComputeSerializedSize;
        }

        @Override // com.google.protobuf.nano.MessageNano
        public Key mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            while (true) {
                int tag = codedInputByteBufferNano.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    int int32 = codedInputByteBufferNano.readInt32();
                    switch (int32) {
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                            this.type = int32;
                            break;
                    }
                } else if (tag == 18) {
                    this.name = codedInputByteBufferNano.readString();
                } else if (tag == 24) {
                    this.id = codedInputByteBufferNano.readInt64();
                } else if (tag == 32) {
                    this.checksum = codedInputByteBufferNano.readInt64();
                } else if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, tag)) {
                    return this;
                }
            }
        }

        @Override // com.google.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            codedOutputByteBufferNano.writeInt32(1, this.type);
            if (!this.name.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.name);
            }
            long j = this.id;
            if (j != 0) {
                codedOutputByteBufferNano.writeInt64(3, j);
            }
            long j2 = this.checksum;
            if (j2 != 0) {
                codedOutputByteBufferNano.writeInt64(4, j2);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }

    public static final class Resource extends MessageNano {
        public byte[] data;
        public int dpi;

        public Resource() {
            clear();
        }

        public Resource clear() {
            this.dpi = 0;
            this.data = WireFormatNano.EMPTY_BYTES;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.google.protobuf.nano.MessageNano
        protected int computeSerializedSize() {
            return super.computeSerializedSize() + CodedOutputByteBufferNano.computeInt32Size(1, this.dpi) + CodedOutputByteBufferNano.computeBytesSize(2, this.data);
        }

        @Override // com.google.protobuf.nano.MessageNano
        public Resource mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            while (true) {
                int tag = codedInputByteBufferNano.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.dpi = codedInputByteBufferNano.readInt32();
                } else if (tag == 18) {
                    this.data = codedInputByteBufferNano.readBytes();
                } else if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, tag)) {
                    return this;
                }
            }
        }

        @Override // com.google.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            codedOutputByteBufferNano.writeInt32(1, this.dpi);
            codedOutputByteBufferNano.writeBytes(2, this.data);
            super.writeTo(codedOutputByteBufferNano);
        }
    }

    public static final class Screen extends MessageNano {
        public long id;
        public int rank;

        public Screen() {
            clear();
        }

        public Screen clear() {
            this.id = 0L;
            this.rank = 0;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.google.protobuf.nano.MessageNano
        protected int computeSerializedSize() {
            int iComputeSerializedSize = super.computeSerializedSize() + CodedOutputByteBufferNano.computeInt64Size(1, this.id);
            int i = this.rank;
            return i != 0 ? iComputeSerializedSize + CodedOutputByteBufferNano.computeInt32Size(2, i) : iComputeSerializedSize;
        }

        @Override // com.google.protobuf.nano.MessageNano
        public Screen mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            while (true) {
                int tag = codedInputByteBufferNano.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.id = codedInputByteBufferNano.readInt64();
                } else if (tag == 16) {
                    this.rank = codedInputByteBufferNano.readInt32();
                } else if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, tag)) {
                    return this;
                }
            }
        }

        @Override // com.google.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            codedOutputByteBufferNano.writeInt64(1, this.id);
            int i = this.rank;
            if (i != 0) {
                codedOutputByteBufferNano.writeInt32(2, i);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }

    public static final class Widget extends MessageNano {
        public boolean configure;
        public Resource icon;
        public String label;
        public Resource preview;
        public String provider;

        public Widget() {
            clear();
        }

        public Widget clear() {
            this.provider = "";
            this.label = "";
            this.configure = false;
            this.icon = null;
            this.preview = null;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.google.protobuf.nano.MessageNano
        protected int computeSerializedSize() {
            int iComputeSerializedSize = super.computeSerializedSize() + CodedOutputByteBufferNano.computeStringSize(1, this.provider);
            if (!this.label.equals("")) {
                iComputeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.label);
            }
            boolean z = this.configure;
            if (z) {
                iComputeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(3, z);
            }
            Resource resource = this.icon;
            if (resource != null) {
                iComputeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(4, resource);
            }
            Resource resource2 = this.preview;
            return resource2 != null ? iComputeSerializedSize + CodedOutputByteBufferNano.computeMessageSize(5, resource2) : iComputeSerializedSize;
        }

        @Override // com.google.protobuf.nano.MessageNano
        public Widget mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            while (true) {
                int tag = codedInputByteBufferNano.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    this.provider = codedInputByteBufferNano.readString();
                } else if (tag == 18) {
                    this.label = codedInputByteBufferNano.readString();
                } else if (tag == 24) {
                    this.configure = codedInputByteBufferNano.readBool();
                } else if (tag == 34) {
                    if (this.icon == null) {
                        this.icon = new Resource();
                    }
                    codedInputByteBufferNano.readMessage(this.icon);
                } else if (tag == 42) {
                    if (this.preview == null) {
                        this.preview = new Resource();
                    }
                    codedInputByteBufferNano.readMessage(this.preview);
                } else if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, tag)) {
                    return this;
                }
            }
        }

        @Override // com.google.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            codedOutputByteBufferNano.writeString(1, this.provider);
            if (!this.label.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.label);
            }
            boolean z = this.configure;
            if (z) {
                codedOutputByteBufferNano.writeBool(3, z);
            }
            Resource resource = this.icon;
            if (resource != null) {
                codedOutputByteBufferNano.writeMessage(4, resource);
            }
            Resource resource2 = this.preview;
            if (resource2 != null) {
                codedOutputByteBufferNano.writeMessage(5, resource2);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
}
