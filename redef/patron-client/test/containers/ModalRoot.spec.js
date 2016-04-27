/* eslint-env mocha */
import expect from 'expect'
import React from 'react'
import TestUtils from 'react-addons-test-utils'
import ModalRoot, { __RewireAPI__ as DefaultExportModalRootRewireApi } from '../../src/frontend/containers/ModalRoot'
import ReactDOM from 'react-dom'
import { IntlProvider } from 'react-intl'
import { createStore } from 'redux'
import rootReducer from '../../src/frontend/reducers'
import { Provider } from 'react-redux'

function setup (propOverrides) {
  const props = {
    modalActions: {},
    modalProps: {},
    ...propOverrides
  }

  const store = createStore(rootReducer)
  store.getState().modal = props

  const output = TestUtils.renderIntoDocument(
    <Provider store={store}>
      <IntlProvider locale='en'>
        <ModalRoot {...props} />
      </IntlProvider>
    </Provider>
  )

  return {
    props: props,
    output: output,
    node: ReactDOM.findDOMNode(output)
  }
}

describe('containers', () => {
  before(() => {
    DefaultExportModalRootRewireApi.__Rewire__('Modal', React.createClass({
      propTypes: {
        children: React.PropTypes.node.isRequired
      },
      render () {
        return (
          <div>{this.props.children}</div>
        )
      }
    }))
  })

  after(() => {
    DefaultExportModalRootRewireApi.__ResetDependency__
  })

  describe('ModalRoot', () => {
    it('hould display nothing if no modal type', () => {
      const { node } = setup()
      expect(node).toBe(null)
    })

    it('should display Login modal', () => {
      const { node } = setup({ modalType: 'LOGIN' })
      expect(node.querySelectorAll("[data-automation-id='login_modal']").length).toBe(1)
    })

    it('should display Reservation modal', () => {
      const { node } = setup({ modalType: 'RESERVATION' })
      expect(node.querySelectorAll("[data-automation-id='reservation_modal']").length).toBe(1)
    })
  })
})
