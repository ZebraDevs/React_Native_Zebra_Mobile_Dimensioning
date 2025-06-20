import { useState, useEffect } from 'react';
import {
  Text,
  View,
  StyleSheet,
  Button,
  Alert,
  TextInput,
  NativeEventEmitter,
  NativeModules,
  ScrollView,
  Modal,
  TouchableOpacity,
} from 'react-native';
import {
  EnableDimension,
  GetDimension,
  DisableDimension,
  GetDimensionParameters,
  SetDimensionParameters,
} from 'react-native-zebra-mobile-dimensioning';

const { ZebraMobileDimensioning } = NativeModules;
const eventEmitter = new NativeEventEmitter(ZebraMobileDimensioning);

type DimensionParameters = {
  DIMENSIONING_UNIT: string;
  REPORT_IMAGE: string;
  TIMEOUT: string;
  [key: string]: string;
};

type PickerKeys = 'DIMENSIONING_UNIT' | 'REPORT_IMAGE'; // Adjusted type

export default function App() {
  const [objectId, setObjectId] = useState('');
  const [dimensions, setDimensions] = useState({
    length: '',
    width: '',
    height: '',
  });

  const [modalVisible, setModalVisible] = useState(false);
  const [pickerModalVisible, setPickerModalVisible] = useState(false);
  const [selectedPicker, setSelectedPicker] = useState<PickerKeys | null>(null);

  const [dimensionParameters, setParameters] = useState<DimensionParameters>({
    DIMENSIONING_UNIT: ZebraMobileDimensioning.CM,
    REPORT_IMAGE: 'false',
    TIMEOUT: '15',
  });

  useEffect(() => {
    const subscriptionCheck = eventEmitter.addListener(
      ZebraMobileDimensioning.DIMENSIONING_EVENT,
      (event) => {
        console.log('Received event from native:', event);

        const action = event[ZebraMobileDimensioning.ACTION];
        const message = event[ZebraMobileDimensioning.RESULT_MESSAGE];
        const resultCode = event[ZebraMobileDimensioning.RESULT_CODE];

        Alert.alert(
          `Message: ${message}`,
          `Code: ${resultCode}, Action: ${action}`
        );

        switch (action) {
          case ZebraMobileDimensioning.INTENT_ACTION_GET_DIMENSION:
            const length = event[ZebraMobileDimensioning.LENGTH] || '';
            const width = event[ZebraMobileDimensioning.WIDTH] || '';
            const height = event[ZebraMobileDimensioning.HEIGHT] || '';
            setDimensions({ length, width, height });
            break;
          case ZebraMobileDimensioning.INTENT_ACTION_GET_DIMENSION_PARAMETER:
            if (resultCode === ZebraMobileDimensioning.SUCCESS) {
              const parameters: DimensionParameters = {
                DIMENSIONING_UNIT:
                  event[ZebraMobileDimensioning.DIMENSIONING_UNIT] ||
                  ZebraMobileDimensioning.CM,
                REPORT_IMAGE: String(
                  event[ZebraMobileDimensioning.REPORT_IMAGE] || 'false'
                ),
                TIMEOUT: String(event[ZebraMobileDimensioning.TIMEOUT] || '15'),
              };
              const notParameters: string[] = [
                ZebraMobileDimensioning.RESULT_CODE,
                ZebraMobileDimensioning.RESULT_MESSAGE,
                ZebraMobileDimensioning.ACTION,
              ];

              Object.keys(event).forEach((key) => {
                if (!parameters[key] && !notParameters.includes(key)) {
                  parameters[key] = String(event[key] || '');
                }
              });

              setParameters(parameters);
              setModalVisible(true);
            }
            break;
        }
      }
    );

    return () => {
      subscriptionCheck.remove();
    };
  }, []);

  const saveDimensionParameters = () => {
    const { DIMENSIONING_UNIT, REPORT_IMAGE, TIMEOUT } = dimensionParameters;

    if (DIMENSIONING_UNIT && REPORT_IMAGE !== undefined && TIMEOUT) {
      SetDimensionParameters({
        DIMENSIONING_UNIT,
        REPORT_IMAGE: REPORT_IMAGE === 'true',
        TIMEOUT: parseInt(TIMEOUT, 10),
      });
    } else {
      Alert.alert(
        'Error',
        'Please ensure all parameters are provided correctly.'
      );
    }

    setModalVisible(false);
  };

  const handleParameterChange = (key: string, value: string) => {
    setParameters((prev) => ({ ...prev, [key]: value }));
  };

  const enableDimension = () => {
    EnableDimension({ MODULE: ZebraMobileDimensioning.PARCEL_MODULE });
  };

  const startDimension = () => {
    GetDimension({ OBJECT_ID: objectId });
  };

  const disableDimension = () => {
    DisableDimension({});
  };

  const getDimensionParameters = () => {
    GetDimensionParameters({});
  };

  const openPickerModal = (pickerType: PickerKeys) => {
    setSelectedPicker(pickerType);
    setPickerModalVisible(true);
  };

  const options: Record<PickerKeys, string[]> = {
    DIMENSIONING_UNIT: [
      ZebraMobileDimensioning.CM,
      ZebraMobileDimensioning.INCH,
    ],
    REPORT_IMAGE: ['true', 'false'],
  };

  return (
    <ScrollView contentContainerStyle={styles.scrollContainer}>
      <View style={styles.container}>
        <Text style={styles.welcome}>
          Welcome to React Native Mobile Dimensioning App
        </Text>
        <Text style={styles.brief}>
          This app allows you to dimension objects using Zebra mobile
          dimensioning technology.
        </Text>
        <Button title="Enable Dimensioning" onPress={enableDimension} />
        <Button title="Get Parameters" onPress={getDimensionParameters} />

        <View style={styles.inputContainer}>
          <TextInput
            style={styles.input}
            placeholder="Type your Object ID"
            value={objectId}
            onChangeText={setObjectId}
            placeholderTextColor="gray"
          />
        </View>

        <View style={styles.volumetricContainer}>
          <Text style={styles.sectionTitle}>Volumetric Dimensions</Text>
          <View style={styles.dimensionRow}>
            <View style={styles.dimensionBox}>
              <Text style={styles.text2}>Length</Text>
              <TextInput
                style={styles.dimensionInput}
                value={dimensions.length}
                editable={false}
                placeholder="Length"
                placeholderTextColor="gray"
              />
            </View>
            <View style={styles.dimensionBox}>
              <Text style={styles.text2}>Width</Text>
              <TextInput
                style={styles.dimensionInput}
                value={dimensions.width}
                editable={false}
                placeholder="Width"
                placeholderTextColor="gray"
              />
            </View>
            <View style={styles.dimensionBox}>
              <Text style={styles.text2}>Height</Text>
              <TextInput
                style={styles.dimensionInput}
                value={dimensions.height}
                editable={false}
                placeholder="Height"
                placeholderTextColor="gray"
              />
            </View>
          </View>
        </View>

        <Button title="Start Dimensioning" onPress={startDimension} />
        <Button title="Disable Dimensioning" onPress={disableDimension} />

        <Modal visible={modalVisible} animationType="slide" transparent={false}>
          <View style={styles.fullScreenModalContainer}>
            <Text style={styles.modalTitle}>Dimension Parameters</Text>
            <ScrollView style={styles.parameterScroll}>
              {Object.entries(dimensionParameters).map(([key, value]) => (
                <View key={key} style={styles.dimensionBox1}>
                  <Text style={styles.text1}>{key}</Text>
                  {['DIMENSIONING_UNIT', 'REPORT_IMAGE', 'TIMEOUT'].includes(
                    key
                  ) ? (
                    key === 'TIMEOUT' ? (
                      <TextInput
                        style={styles.dimensionInput}
                        value={value}
                        onChangeText={(text) =>
                          handleParameterChange(key, text.replace(/\D/g, ''))
                        }
                        placeholder={`Enter ${key}`}
                        placeholderTextColor="gray"
                        keyboardType="numeric"
                      />
                    ) : (
                      <TouchableOpacity
                        style={styles.pickerButton}
                        onPress={() => openPickerModal(key as PickerKeys)}
                      >
                        <Text style={styles.pickerButtonText}>{value}</Text>
                      </TouchableOpacity>
                    )
                  ) : (
                    <Text style={styles.readOnlyText}>{value}</Text>
                  )}
                </View>
              ))}
            </ScrollView>
            <View style={styles.buttonRow}>
              <Button title="Set Param" onPress={saveDimensionParameters} />
              <Button title="Cancel" onPress={() => setModalVisible(false)} />
            </View>
          </View>
        </Modal>

        {/* Custom Picker Modal */}
        <Modal
          visible={pickerModalVisible}
          animationType="fade"
          transparent={true}
        >
          <View style={styles.modalBackground}>
            <View style={styles.modalContainer}>
              {selectedPicker &&
                options[selectedPicker].map((option) => (
                  <TouchableOpacity
                    key={option}
                    style={styles.optionButton}
                    onPress={() => {
                      handleParameterChange(selectedPicker, option);
                      setPickerModalVisible(false);
                    }}
                  >
                    <Text style={styles.optionText}>{option}</Text>
                  </TouchableOpacity>
                ))}
              <Button
                title="Cancel"
                onPress={() => setPickerModalVisible(false)}
              />
            </View>
          </View>
        </Modal>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  scrollContainer: {
    flexGrow: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 16,
    backgroundColor: '#000',
  },
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 16,
    backgroundColor: '#000',
  },
  welcome: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 5,
    textAlign: 'center',
    color: '#fff',
  },
  brief: {
    fontSize: 14,
    marginBottom: 20,
    textAlign: 'center',
    color: '#fff',
  },
  text1: {
    color: '#fff',
    marginBottom: 5,
  },
  text2: {
    textAlign: 'center',
    color: '#fff',
    marginBottom: 5,
  },
  inputContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 20,
    width: '100%',
  },
  input: {
    flex: 1,
    height: 40,
    borderColor: 'gray',
    borderWidth: 1,
    marginVertical: 5,
    paddingHorizontal: 8,
    color: '#fff',
    backgroundColor: '#1a1a1a',
  },
  volumetricContainer: {
    marginTop: 30,
    width: '50%',
    alignItems: 'center',
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 10,
    color: '#fff',
  },
  dimensionRow: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    width: '100%',
    marginBottom: 10,
  },
  dimensionBox: {
    alignItems: 'center',
    width: '80%',
    marginVertical: 10,
  },
  dimensionBox1: {
    justifyContent: 'center',
    width: '80%',
    marginVertical: 10,
  },
  dimensionInput: {
    height: 40,
    borderColor: 'gray',
    borderWidth: 1,
    paddingHorizontal: 0,
    width: '80%',
    textAlign: 'center',
    color: '#fff',
    backgroundColor: '#1a1a1a',
    fontSize: 16,
  },
  modalBackground: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
  },
  modalContainer: {
    width: '95%',
    maxHeight: '80%',
    backgroundColor: '#333',
    borderRadius: 10,
    padding: 10,
    alignItems: 'center',
  },
  fullScreenModalContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#333',
    padding: 20,
  },
  parameterScroll: {
    flex: 1,
    width: '100%',
  },
  modalTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    marginBottom: 20,
    color: '#fff',
    textAlign: 'center',
  },
  pickerButton: {
    backgroundColor: '#1a1a1a',
    padding: 10,
    borderRadius: 5,
    marginVertical: 5,
    width: '80%',
  },
  pickerButtonText: {
    color: '#fff',
    textAlign: 'center',
  },
  optionButton: {
    backgroundColor: '#1a1a1a',
    padding: 10,
    borderRadius: 5,
    marginVertical: 5,
    width: '80%',
  },
  optionText: {
    color: '#fff',
    textAlign: 'center',
  },
  readOnlyText: {
    color: '#fff',
    backgroundColor: '#1a1a1a',
    padding: 10,
    borderRadius: 5,
    marginVertical: 5,
    width: '80%',
    textAlign: 'center',
  },
  buttonRow: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    width: '100%',
    paddingHorizontal: 20,
    marginBottom: 20,
  },
});
