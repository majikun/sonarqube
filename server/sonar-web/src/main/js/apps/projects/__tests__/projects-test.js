/*
 * SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import React from 'react';
import TestUtils from 'react-addons-test-utils';
import { expect } from 'chai';
import sinon from 'sinon';

import Projects from '../projects';

describe('Projects', function () {
  describe('Projects', () => {
    it('should render list of projects with no selection', () => {
      const projects = [
        { id: '1', key: 'a', name: 'A', qualifier: 'TRK' },
        { id: '2', key: 'b', name: 'B', qualifier: 'TRK' }
      ];

      const result = TestUtils.renderIntoDocument(
          <Projects projects={projects} selection={[]} refresh={sinon.spy()}/>);
      expect(TestUtils.scryRenderedDOMComponentsWithTag(result, 'tr')).to.have.length(2);
      expect(TestUtils.scryRenderedDOMComponentsWithClass(result, 'icon-checkbox-checked')).to.be.empty;
    });

    it('should render list of projects with one selected', () => {
      const projects = [
        { id: '1', key: 'a', name: 'A', qualifier: 'TRK' },
        { id: '2', key: 'b', name: 'B', qualifier: 'TRK' }
      ];
      const selection = ['1'];

      const result = TestUtils.renderIntoDocument(
          <Projects projects={projects} selection={selection} refresh={sinon.spy()}/>);
      expect(TestUtils.scryRenderedDOMComponentsWithTag(result, 'tr')).to.have.length(2);
      expect(TestUtils.scryRenderedDOMComponentsWithClass(result, 'icon-checkbox-checked')).to.have.length(1);
    });
  });
});
