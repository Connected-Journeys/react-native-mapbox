import React from 'react';
import {View, Text, StyleSheet, Image, Button} from 'react-native';
import RNMapBox from 'react-native-mapbox';

import bus from '../assets/bus.png';
import train from '../assets/train.png';
import {commonStyles} from './styles';

const marker1 = {
  id: 'Marker1',
  lat: -36.8485,
  lng: 174.7633,
  title: 'Custom',
  subtitle: 'Sub1',
  icon: {...Image.resolveAssetSource(bus), scale: 2.25},
  label: {
    labelText: '27W',
    color: '#00AD16',
    textSize: 11,
    frame: {
      x: 10,
      y: 27,
      width: 25,
      height: 14,
    },
    backgroundColor: '#00AD16',
    border: {
      color: '#00AD16',
      width: 0.05,
      cornerRadius: 5.0,
    },
  },
};

const marker2 = {
  id: 'Marker2',
  lat: -37.787,
  lng: 175.2793,
  title: 'Custom2',
  subtitle: 'Sub2',
  icon: Image.resolveAssetSource(bus),
  pulsator: {
    color: '#3089d3',
  },
  label: {
    labelText: '27W',
    color: '#00AD16',
    textSize: 11,
    frame: {
      x: 10,
      y: 27,
      width: 25,
      height: 14,
    },
    backgroundColor: '#00AD16',
    border: {
      color: '#00AD16',
      width: 0.05,
      cornerRadius: 5.0,
    },
  },
};

const marker3 = {
  id: 'Marker3',
  lat: -37.971794,
  lng: 174.794618,
  icon: Image.resolveAssetSource(train),
};

const marker4 = {
  id: 'Marker4',
  lat: -39.071666,
  lng: 174.161978,
  icon: Image.resolveAssetSource(train),
};

const marker5 = {
  id: 'Marker5',
  lat: -38.720623,
  lng: 176.089187,
  icon: Image.resolveAssetSource(train),
};

const marker6 = {
  id: 'Marker6',
  lat: -38.174694,
  lng: 176.259062,
  icon: Image.resolveAssetSource(train),
};

const marker7 = {
  id: 'Marker7',
  lat: -37.860884,
  lng: 176.616387,
  icon: Image.resolveAssetSource(train),
};

export default class Markers extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      markers: [marker1],
    };
    this.updateMarkers = this.updateMarkers.bind(this);
  }

  updateMarkers() {
    window.setInterval(() => {
      const marker = this.state.markers[0];

      this.setState({
        markers: [
          {
            id: 'Marker2',
            lat: marker.lat + 0.01,
            lng: marker.lng + 0.01,
            title: 'Custom',
            subtitle: 'Sub1',
            icon: Image.resolveAssetSource(bus),
            pulsator: {
              color: '#3089d3',
            },
          },
          marker1,
          marker3,
          marker4,
          marker5,
          marker6,
          marker7,
        ],
      });
    }, 1000);
  }

  componentDidMount() {
    window.setTimeout(() => {
      this.setState({
        markers: [marker2],
      });
      this.updateMarkers();
    }, 3000);
  }

  render() {
    return (
      <>
        <RNMapBox
          camera={{
            target: {
              lat: -37.787,
              lng: 175.2793,
            },
            zoom: 7,
          }}
          style={StyleSheet.absoluteFillObject}
          onMapReady={() => console.log('onMapReady callback')}
          markers={this.state.markers}
        />
        <View style={commonStyles.backButton}>
          <Button title="<Back" onPress={this.props.onGoBack} />
        </View>
      </>
    );
  }
}
